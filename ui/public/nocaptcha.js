!function(){"use strict";!function(e){const t={onInit:null,onVerify:null},n={"Content-Type":"application/json"},r="http://localhost:9080";class s{static base64UrlEncode(e){return btoa(String.fromCharCode(...new Uint8Array(e))).replace(/\+/g,"-").replace(/\//g,"_").replace(/=+$/,"")}static base64UrlDecode(e){let t=e.replace(/-/g,"+").replace(/_/g,"/");for(;t.length%4;)t+="=";const n=atob(t);return new Uint8Array([...n].map((e=>e.charCodeAt(0)))).buffer}}class o{constructor(e,t){this.httpCode=e,this.message=t}static fromJson(e){const t=e.errorCode;return new o(t.httpCode,t.message)}}class a{constructor(e,t,n,r){this.value=e,this.code=t,this.errorMessage=n,this.errorCode=r}isSuccess(){return void 0!==this.value}isFailure(){return void 0!==this.errorCode}static success(e){return new a(e)}static failure(e){const t=o.fromJson(e);return new a(void 0,void 0,e.message,t)}static failureSorry(){const e=new o(-1,"Generic Failure");return new a(void 0,void 0,"Sorry, something went wrong. Please try later.",e)}static failureNetwork(){const e=new o(-2,"Network Failure");return new a(void 0,void 0,"Sorry, it looks like your internet connection is unstable. Please try later.",e)}}class i extends HTMLElement{constructor(){super(),this.attachShadow({mode:"open"});const e=document.createElement("style");e.textContent='\n        .flex {\n          display: flex;\n        }\n\n        .flex-1 {\n          flex: 1 1 0%;\n        }\n\n        .flex-col {\n          flex-direction: column;\n        }\n\n        .justify-center {\n          justify-content: center;\n        }\n\n        .items-center {\n          align-items: center;\n        }\n\n        .bg-white {\n          background-color: rgb(255 255 255);\n        }\n\n        .shadow {\n          box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1), 0 1px 2px -1px rgb(0 0 0 / 0.1);\n        }\n\n        .border {\n          border-width: 1px;\n        }\n\n        .border-solid {\n          border-style: solid;\n        }\n\n        .border-slate-300 {\n          border-color: #e0e7ee;\n        }\n\n        .rounded-xl {\n          border-radius: 0.75rem;\n        }\n\n        .p-2 {\n          padding: 0.5rem;\n        }\n\n        .py-3 {\n          padding-top: 0.875rem; /* 14px */\n          padding-bottom: 0.875rem; /* 14px */\n        }\n\n        .px-4 {\n          padding-left: 1rem; /* 16px */\n          padding-right: 1rem; /* 16px */\n        }\n\n        .mx-auto {\n          margin-left: auto;\n          margin-right: auto;\n        }\n\n        .my-2 {\n          margin-top: 0.5rem; /* 8px */\n          margin-bottom: 0.5rem; /* 8px */\n        }\n\n        .mr-3 {\n          margin-right: 0.75rem; /* 12px */\n        }\n\n        .text-xl {\n          font-size: 1.25rem; /* 20px */\n          line-height: 1.75rem; /* 28px */\n        }\n\n        .text-2xl {\n          font-size: 1.5rem; /* 24px */\n          line-height: 2rem; /* 32px */\n        }\n\n        .text-xs {\n          font-size: 0.75rem; /* 12px */\n          line-height: 1rem; /* 16px */\n        }\n\n        .text-default {\n          color: #0a2342;\n          font-family: ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;\n        }\n\n        .text-gray-500 {\n          color: rgb(107 114 128);\n        }\n\n        .text-center {\n          text-align: center;\n        }\n\n        .underline {\n          text-decoration-line: underline;\n        }\n\n        .w-8 {\n          width: 2rem; /* 32px */\n        }\n\n        .h-8 {\n          height: 2rem; /* 32px */\n        }\n\n        .visited:visited {\n          color: rgb(107 114 128);\n        }\n\n        .max-w-360 {\n          max-width: 360px;\n        }\n\n        .cursor-pointer {\n          cursor: pointer;\n        }\n      ';const t=document.createElement("div");t.className="container",t.innerHTML='\n        <div class="flex flex-1 justify-center items-center bg-white text-default max-w-360 mx-auto">\n            <div class="flex flex-col flex-1">\n              <div class="flex flex-col flex-1 shadow border rounded-xl">\n                <div class="flex items-center py-3 px-4">\n                  <button id="verifyBtn" class="cursor-pointer flex items-center p-2 mr-3 border border-solid border-slate-300 rounded-xl bg-white text-default">\n                    <svg class="w-8 h-8" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 14 14"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><path d="M7 13.39a5 5 0 0 0 5-5V5.61a5 5 0 0 0-1.27-3.33M2 6.72v1.67A5 5 0 0 0 5.06 13M9.5 1.28a5 5 0 0 0-6.83 1.83a4.9 4.9 0 0 0-.57 1.52"/><path d="M6.48 3.51A2.51 2.51 0 0 1 9.5 6v1.61m-.64 2.1A2.5 2.5 0 0 1 4.5 8V6a2.5 2.5 0 0 1 .2-1M7 6.11v1.67"/></g></svg>\n                  </button>\n                  <p class="text-xl"><span class="text-2xl">👈</span> Confirm you are a human.</p>\n                </div>\n              </div>\n              <p class="text-center text-xs text-gray-500 my-2">\n                  Private by <a class="underline visited" href="https://singlr.ai" target="_blank">design</a>. No data is tracked, saved, or shared.\n              </p>\n            </div>\n        </div>\n      ',this.shadowRoot.appendChild(e),this.shadowRoot.appendChild(t),this.shadowRoot.getElementById("verifyBtn").addEventListener("click",(()=>this.startVerification()));this.shadowRoot.getElementById("verifyBtn").innerHTML='\n        <svg class="w-8 h-8 spinner hidden" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 24 24"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round" stroke-width="2"><path stroke-dasharray="16" stroke-dashoffset="16" d="M12 3c4.97 0 9 4.03 9 9"><animate fill="freeze" attributeName="stroke-dashoffset" dur="0.3s" values="16;0"/><animateTransform attributeName="transform" dur="1.5s" repeatCount="indefinite" type="rotate" values="0 12 12;360 12 12"/></path><path stroke-dasharray="64" stroke-dashoffset="64" stroke-opacity="0.3" d="M12 3c4.97 0 9 4.03 9 9c0 4.97 -4.03 9 -9 9c-4.97 0 -9 -4.03 -9 -9c0 -4.97 4.03 -9 9 -9Z"><animate fill="freeze" attributeName="stroke-dashoffset" dur="1.2s" values="64;0"/></path></g></svg>\n            <svg class="w-8 h-8 default-icon" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 14 14"><g fill="none" stroke="currentColor" stroke-linecap="round" stroke-linejoin="round"><path d="M7 13.39a5 5 0 0 0 5-5V5.61a5 5 0 0 0-1.27-3.33M2 6.72v1.67A5 5 0 0 0 5.06 13M9.5 1.28a5 5 0 0 0-6.83 1.83a4.9 4.9 0 0 0-.57 1.52"/><path d="M6.48 3.51A2.51 2.51 0 0 1 9.5 6v1.61m-.64 2.1A2.5 2.5 0 0 1 4.5 8V6a2.5 2.5 0 0 1 .2-1M7 6.11v1.67"/></g></svg>\n          ';const n=document.createElement("div");n.id="error-message",n.className="error-message hidden text-red-500 text-sm mt-2 text-center",n.textContent="Something went wrong. Please try again.",this.shadowRoot.querySelector(".container").appendChild(n);this.shadowRoot.querySelector("style").textContent+="\n            .hidden {\n              display: none;\n            }\n\n            .spinner {\n              animation: spin 1s linear infinite;\n            }\n\n            @keyframes spin {\n              from {\n                transform: rotate(0deg);\n              }\n              to {\n                transform: rotate(360deg);\n              }\n            }\n\n            .text-red-500 {\n              color: #e53e3e;\n            }\n\n            button:disabled {\n              opacity: 0.5;\n              cursor: not-allowed;\n            }\n          "}connectedCallback(){this.dataset.init&&(t.onInit=e[this.dataset.init]),this.dataset.verified&&(t.onVerify=e[this.dataset.verified]),t.onInit&&t.onInit()}setLoading(e){const t=this.shadowRoot.getElementById("verifyBtn"),n=t.querySelector(".spinner"),r=t.querySelector(".default-icon"),s=this.shadowRoot.getElementById("error-message");e?(n.classList.remove("hidden"),r.classList.add("hidden"),t.disabled=!0,s.classList.add("hidden")):(n.classList.add("hidden"),r.classList.remove("hidden"),t.disabled=!1)}setGenericError(){this.shadowRoot.getElementById("error-message").classList.remove("hidden")}async startVerification(){const n=localStorage.getItem("no-captcha-id");this.setLoading(!0);var r=await e.NoCaptcha.api.captchaStart(!0);if(this.setLoading(!1),r.isFailure())return console.error(r.errorMessage),void this.setGenericError();try{const o=await navigator.credentials.create({publicKey:r.value.credentialsOptions}),a=o.getClientExtensionResults(),i={id:o.id,rawId:s.base64UrlEncode(o.rawId),response:{clientDataJSON:s.base64UrlEncode(o?.response.clientDataJSON),attestationObject:s.base64UrlEncode(o?.response.attestationObject)},authenticatorAttachment:o.authenticatorAttachment,type:o.type,clientExtensionResults:a};if(this.setLoading(!0),r=await e.NoCaptcha.api.captchaComplete(r.value.base64Id,i),this.setLoading(!1),r.isFailure())return console.error(r.errorMessage),void this.setGenericError();localStorage.setItem("no-captcha-id",n),t.onVerify&&t.onVerify(r)}catch(e){console.error(e)}}}customElements.get("no-captcha")||customElements.define("no-captcha",i),e.NoCaptcha={api:new class{async captchaStart(){const e=r+"/v1/nocaptcha/start";try{const t={id:"Anonymous"},r=await fetch(e,{method:"POST",headers:n,body:JSON.stringify(t)});if(201===r.status){const e=await r.json(),t=JSON.parse(e.pubKeyCredOpts).publicKey,n=t.user.id;if(t.challenge=s.base64UrlDecode(t.challenge),t.user.id=s.base64UrlDecode(t.user.id),t.excludeCredentials)for(let e=0;e<t.excludeCredentials.length;e++)t.excludeCredentials[e].id=s.base64UrlDecode(t.excludeCredentials[e].id);return a.success({credentialsOptions:t,base64Id:n})}return a.failure(await r.json())}catch(e){return console.error(e),a.failureSorry()}}async captchaComplete(e,t){const s=r+"/v1/nocaptcha/complete";try{const r={id:e,pubKeyCredOpts:t},o=await fetch(s,{method:"PUT",headers:n,body:JSON.stringify(r)});if(202===o.status){const e=await o.json();return a.success(e)}return a.failure(await o.json())}catch(e){return a.failureSorry()}}},init:function(e={}){e.onInit&&(t.onInit=e.onInit),e.onVerify&&(t.onVerify=e.onVerify),t.onInit&&t.onInit()}}}(window)}();