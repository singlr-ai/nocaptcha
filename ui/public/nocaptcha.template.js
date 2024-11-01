(function (window) {
  "use strict";

  // Store callbacks in a scope accessible to both the class and the global interface
  const callbacks = {
    onInit: null,
    onVerify: null,
  };

  const JSON_HEADERS = {
    "Content-Type": "application/json",
  };

  const BASE_URI = import.meta.env.VITE_API_URL;

  const firstCallback = function () {
    if (callbacks.onInit) callbacks.onInit();

    const id = sessionStorage.getItem("no-captcha-id");
    if (id) {
      if (callbacks.onVerify) callbacks.onVerify();
    }
  };

  class Utils {
    static base64UrlEncode(value) {
      return btoa(String.fromCharCode(...new Uint8Array(value)))
        .replace(/\+/g, "-")
        .replace(/\//g, "_")
        .replace(/=+$/, "");
    }

    static base64UrlDecode(value) {
      let base64 = value.replace(/-/g, "+").replace(/_/g, "/");
      // Pad the base64 string with "=" to make the string length a multiple of 4
      while (base64.length % 4) {
        base64 += "=";
      }
      const raw = atob(base64);
      return new Uint8Array([...raw].map((c) => c.charCodeAt(0))).buffer;
    }
  }

  class ErrorCode {
    constructor(httpCode, message) {
      this.httpCode = httpCode;
      this.message = message;
    }

    static fromJson(json) {
      const errorCode = json["errorCode"];
      return new ErrorCode(errorCode["httpCode"], errorCode["message"]);
    }
  }

  class Result {
    constructor(value, code, errorMessage, errorCode) {
      this.value = value;
      this.code = code;
      this.errorMessage = errorMessage;
      this.errorCode = errorCode;
    }

    isSuccess() {
      return this.value !== undefined;
    }

    isFailure() {
      return this.errorCode !== undefined;
    }

    static success(model) {
      return new Result(model);
    }

    static failure(json) {
      const error = ErrorCode.fromJson(json);
      return new Result(undefined, undefined, json["message"], error);
    }

    static failureSorry() {
      const error = new ErrorCode(-1, "Generic Failure");
      return new Result(
        undefined,
        undefined,
        "Sorry, something went wrong. Please try later.",
        error,
      );
    }

    static failureNetwork() {
      const error = new ErrorCode(-2, "Network Failure");
      return new Result(
        undefined,
        undefined,
        "Sorry, it looks like your internet connection is unstable. Please try later.",
        error,
      );
    }
  }

  class Api {
    async captchaStart() {
      const apiUrl = BASE_URI + "/v1/nocaptcha/start";

      try {
        const body = {
          id: "Anonymous",
        };

        const response = await fetch(apiUrl, {
          method: "POST",
          headers: JSON_HEADERS,
          body: JSON.stringify(body),
        });

        if (response.status === 201) {
          const json = await response.json();

          const pubKeyCredOpts = JSON.parse(json["pubKeyCredOpts"]).publicKey;
          const base64Id = pubKeyCredOpts.user.id;
          pubKeyCredOpts.challenge = Utils.base64UrlDecode(
            pubKeyCredOpts.challenge,
          );
          pubKeyCredOpts.user.id = Utils.base64UrlDecode(
            pubKeyCredOpts.user.id,
          );

          if (pubKeyCredOpts.excludeCredentials) {
            for (let i = 0; i < pubKeyCredOpts.excludeCredentials.length; i++) {
              pubKeyCredOpts.excludeCredentials[i].id = Utils.base64UrlDecode(
                pubKeyCredOpts.excludeCredentials[i].id,
              );
            }
          }

          return Result.success({
            credentialsOptions: pubKeyCredOpts,
            base64Id: base64Id,
          });
        } else {
          return Result.failure(await response.json());
        }
      } catch (error) {
        console.error(error);
        return Result.failureSorry();
      }
    }

    async captchaComplete(id, credential) {
      const apiUrl = BASE_URI + "/v1/nocaptcha/complete";

      try {
        const body = {
          id: id,
          pubKeyCredOpts: credential,
        };

        const response = await fetch(apiUrl, {
          method: "PUT",
          headers: JSON_HEADERS,
          body: JSON.stringify(body),
        });

        if (response.status === 202) {
          const json = await response.json();
          return Result.success(json);
        } else {
          return Result.failure(await response.json());
        }
      } catch (error) {
        return Result.failureSorry();
      }
    }
  }

  class NoCaptcha extends HTMLElement {
    constructor() {
      super();
      this.attachShadow({ mode: "open" });

      // Create styles
      const style = document.createElement("style");
      style.textContent = `
        .flex {
          display: flex;
        }

        .flex-1 {
          flex: 1 1 0%;
        }

        .flex-col {
          flex-direction: column;
        }

        .justify-center {
          justify-content: center;
        }

        .items-center {
          align-items: center;
        }

        .bg-white {
          background-color: rgb(255 255 255);
        }

        .shadow {
          box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);
        }

        .border {
          border-width: 1px;
        }

        .border-solid {
          border-style: solid;
        }

        .border-slate-300 {
          border-color: #e0e7ee;
        }

        .rounded-xl {
          border-radius: 0.75rem;
        }

        .p-2 {
          padding: 0.5rem;
        }

        .py-3 {
          padding-top: 0.875rem; /* 14px */
          padding-bottom: 0.875rem; /* 14px */
        }

        .px-4 {
          padding-left: 1rem; /* 16px */
          padding-right: 1rem; /* 16px */
        }

        .mx-auto {
          margin-left: auto;
          margin-right: auto;
        }

        .my-2 {
          margin-top: 0.5rem; /* 8px */
          margin-bottom: 0.5rem; /* 8px */
        }

        .mr-3 {
          margin-right: 0.75rem; /* 12px */
        }

        .text-xl {
          font-size: 1.25rem; /* 20px */
          line-height: 1.75rem; /* 28px */
        }

        .text-2xl {
          font-size: 1.5rem; /* 24px */
          line-height: 2rem; /* 32px */
        }

        .text-xs {
          font-size: 0.75rem; /* 12px */
          line-height: 1rem; /* 16px */
        }

        .text-default {
          color: #0a2342;
          font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
        }

        .text-gray-500 {
          color: rgb(107 114 128);
        }

        .text-center {
          text-align: center;
        }

        .underline {
          text-decoration-line: underline;
        }

        .w-8 {
          width: 2rem; /* 32px */
        }

        .h-8 {
          height: 2rem; /* 32px */
        }

        .visited:visited {
          color: rgb(107 114 128);
        }

        .max-w-360 {
          max-width: 360px;
        }

        .cursor-pointer {
          cursor: pointer;
        }
      `;

      // Create HTML structure
      const container = document.createElement("div");
      container.className = "container";
      container.innerHTML = `
        <div class="flex flex-1 justify-center items-center bg-white text-default max-w-360 mx-auto">
            <div class="flex flex-col flex-1">
              <div class="flex flex-col flex-1 shadow border rounded-xl">
                <div class="flex items-center py-3 px-4">
                  <button id="verifyBtn" class="cursor-pointer flex items-center p-2 mr-3 border border-solid border-slate-300 rounded-xl bg-white text-default">
                    <svg class="w-8 h-8" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 14 14"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><path d="M7 13.39a5 5 0 0 0 5-5V5.61a5 5 0 0 0-1.27-3.33M2 6.72v1.67A5 5 0 0 0 5.06 13M9.5 1.28a5 5 0 0 0-6.83 1.83a4.9 4.9 0 0 0-.57 1.52"/><path d="M6.48 3.51A2.51 2.51 0 0 1 9.5 6v1.61m-.64 2.1A2.5 2.5 0 0 1 4.5 8V6a2.5 2.5 0 0 1 .2-1M7 6.11v1.67"/></g></svg>
                  </button>
                  <p class="text-xl"><span class="text-2xl">👈</span> Confirm you are a human.</p>
                </div>
              </div>
              <p class="text-center text-xs text-gray-500 my-2">
                  Private by <a class="underline visited" href="https://singlr.ai" target="_blank">design</a>. No data is tracked, saved, or shared.
              </p>
            </div>
        </div>
      `;

      this.shadowRoot.appendChild(style);
      this.shadowRoot.appendChild(container);

      this.shadowRoot
        .getElementById("verifyBtn")
        .addEventListener("click", () => this.startVerification());

      // Add spinner SVG (initially hidden)
      const button = this.shadowRoot.getElementById("verifyBtn");
      button.innerHTML = `
        <svg class="w-8 h-8 spinner hidden" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"><path stroke-dasharray="16" stroke-dashoffset="16" d="M12 3c4.97 0 9 4.03 9 9"><animate fill="freeze" attributeName="stroke-dashoffset" dur="0.3s" values="16;0"/><animateTransform attributeName="transform" dur="1.5s" repeatCount="indefinite" type="rotate" values="0 12 12;360 12 12"/></path><path stroke-dasharray="64" stroke-dashoffset="64" stroke-opacity="0.3" d="M12 3c4.97 0 9 4.03 9 9c0 4.97 -4.03 9 -9 9c-4.97 0 -9 -4.03 -9 -9c0 -4.97 4.03 -9 9 -9Z"><animate fill="freeze" attributeName="stroke-dashoffset" dur="1.2s" values="64;0"/></path></g></svg>
            <svg class="w-8 h-8 default-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 14 14"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><path d="M7 13.39a5 5 0 0 0 5-5V5.61a5 5 0 0 0-1.27-3.33M2 6.72v1.67A5 5 0 0 0 5.06 13M9.5 1.28a5 5 0 0 0-6.83 1.83a4.9 4.9 0 0 0-.57 1.52"/><path d="M6.48 3.51A2.51 2.51 0 0 1 9.5 6v1.61m-.64 2.1A2.5 2.5 0 0 1 4.5 8V6a2.5 2.5 0 0 1 .2-1M7 6.11v1.67"/></g></svg>
          `;

      // Add error message element (initially hidden)
      const errorDiv = document.createElement("div");
      errorDiv.id = "error-message";
      errorDiv.className =
        "error-message hidden text-red-500 text-sm mt-2 text-center";
      errorDiv.textContent = "Something went wrong. Please try again.";
      this.shadowRoot.querySelector(".container").appendChild(errorDiv);

      const additionalStyles = `
            .hidden {
              display: none;
            }

            .spinner {
              animation: spin 1s linear infinite;
            }

            @keyframes spin {
              from {
                transform: rotate(0deg);
              }
              to {
                transform: rotate(360deg);
              }
            }

            .text-red-500 {
              color: #e53e3e;
            }

            button:disabled {
              opacity: 0.5;
              cursor: not-allowed;
            }
          `;
      this.shadowRoot.querySelector("style").textContent += additionalStyles;
      // Get callback functions from data attributes
      if (this.dataset.init) callbacks.onInit = window[this.dataset.init];
      if (this.dataset.verified)
        callbacks.onVerify = window[this.dataset.verified];
      firstCallback();
    }

    setLoading(isLoading) {
      const button = this.shadowRoot.getElementById("verifyBtn");
      const spinner = button.querySelector(".spinner");
      const defaultIcon = button.querySelector(".default-icon");
      const errorMessage = this.shadowRoot.getElementById("error-message");

      if (isLoading) {
        spinner.classList.remove("hidden");
        defaultIcon.classList.add("hidden");
        button.disabled = true;
        errorMessage.classList.add("hidden");
      } else {
        spinner.classList.add("hidden");
        defaultIcon.classList.remove("hidden");
        button.disabled = false;
      }
    }

    setGenericError() {
      const errorMessage = this.shadowRoot.getElementById("error-message");
      errorMessage.classList.remove("hidden");
    }

    async startVerification() {
      this.setLoading(true);
      var result = await window.NoCaptcha.api.captchaStart(true);
      this.setLoading(false);
      if (result.isFailure()) {
        console.error(result.errorMessage);
        this.setGenericError();
        return;
      }

      try {
        const credential = await navigator.credentials.create({
          publicKey: result.value.credentialsOptions,
        });
        const extensionResults = credential.getClientExtensionResults();

        const decodedCredentials = {
          id: credential.id,
          rawId: Utils.base64UrlEncode(credential.rawId),
          response: {
            clientDataJSON: Utils.base64UrlEncode(
              credential?.response.clientDataJSON,
            ),
            attestationObject: Utils.base64UrlEncode(
              credential?.response.attestationObject,
            ),
          },
          authenticatorAttachment: credential.authenticatorAttachment,
          type: credential.type,
          clientExtensionResults: extensionResults,
        };

        this.setLoading(true);
        const base64Id = result.value.base64Id;
        result = await window.NoCaptcha.api.captchaComplete(
          base64Id,
          decodedCredentials,
        );
        this.setLoading(false);
        if (result.isFailure()) {
          console.error(result.errorMessage);
          this.setGenericError();
          return;
        } else {
          sessionStorage.setItem("no-captcha-id", base64Id);
          if (callbacks.onVerify) callbacks.onVerify(result);
        }
      } catch (e) {
        console.error(e);
      }
    }
  }

  // Register the custom element
  if (!customElements.get("no-captcha")) {
    customElements.define("no-captcha", NoCaptcha);
  }

  window.NoCaptcha = {
    api: new Api(),

    // Initialize the SDK
    init: function (options = {}) {
      if (options.onInit) callbacks.onInit = options.onInit;
      if (options.onVerify) callbacks.onVerify = options.onVerify;

      firstCallback();
    },
  };
})(window);
