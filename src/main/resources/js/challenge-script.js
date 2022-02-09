document.head.insertAdjacentHTML("beforeend", "<style>.px-container{align-items:center;display:flex;flex:1;justify-content:space-between;flex-direction:column;height:100}.px-container>div{width:100;display:flex;justify-content:center}.px-container>div>div{display:flex;width:80}.page-title-wrapper{flex-grow:2}.page-title{flex-direction:column-reverse}.content-wrapper{flex-grow:5}.content{flex-direction:column}.page-footer-wrapper{align-items:center;flex-grow:.2;background-color:#000;color:#c5c5c5;font-size:70}.content p{margin:14px 0;}</style>");
var submitted = true;

function createElement(type, classes, attributes, styles) {
    const elm = document.createElement(type);
    if (classes && classes.length > 0) {
        classes.forEach((e) => {
            elm.classList.add(e);
        });
    }
    if (attributes) {
        Object.keys(attributes).forEach((k) => {
            elm.setAttribute(k, attributes[k]);
        });
    }
    if (styles) {
         Object.keys(styles).forEach((k) => {
             elm.style[k] = styles[k];
         });
    }
    return elm;
}
function insertAfter(el, referenceNode) {
    referenceNode.parentNode.insertBefore(el, referenceNode.nextSibling);
}

function callback() {
    const container = document.createElement("div");
    container.classList.add("px-container");
    const pageTitleWrapper = createElement('div', ['page-title-wrapper']);
    const pageTitle = createElement('div', ['page-title']);
    const title = createElement('h1');
    title.innerText = 'Please verify you are a human';
    pageTitle.appendChild(title);
    pageTitleWrapper.appendChild(pageTitle);
    const contentWrapper = createElement('div', ['content-wrapper']);
    const content = createElement('div', ['content']);
    const pxCaptcha = createElement('div', null, { id: 'px-captcha' });
    content.appendChild(pxCaptcha);
    const p1 = createElement('p');
    p1.innerText = 'Access to this page has been denied because we believe you are using automation tools to browse the website.';
    content.appendChild(p1);
    const p2 = createElement('p');
    p2.innerText = 'This may happen as a result of the following:';
    content.appendChild(p2);
    const ul = createElement('ul');
    const li1 = createElement('li');
    li1.innerText = 'Javascript is disabled or blocked by an extension (ad blockers for example)';
    ul.appendChild(li1);
    const li2 = createElement('li');
    li2.innerText = 'Your browser does not support cookies';
    ul.appendChild(li2);
    content.appendChild(ul);
    const p3 = createElement('p');
    p3.innerText = 'Please make sure that Javascript and cookies are enabled on your browser and that you are not blocking them from loading.';
    content.appendChild(p3);
    const p4 = createElement('p');
    p4.innerText = "Reference ID: # %1$s";
    content.appendChild(p4);
    contentWrapper.appendChild(content);
    const pageFooterWrapper = createElement('div', ['page-footer-wrapper']);
    const pageFooter = createElement('div', ['page-footer']);
    const preLink = createElement('span');
    preLink.innerText = 'Powered by';
    const link = createElement('a', null, { href: 'https://www.perimeterx.com/whywasiblocked' });
    link.innerText = 'PerimeterX';
    const postLink = createElement('span');
    postLink.innerText = ' , Inc.';

    pageFooter.appendChild(preLink);
    pageFooter.appendChild(link);
    pageFooter.appendChild(postLink);
    pageFooterWrapper.appendChild(pageFooter);

    container.appendChild(pageTitleWrapper);
    container.appendChild(contentWrapper);
    container.appendChild(pageFooterWrapper);
    var anchor = null;
    if (typeof loginHelpers !== 'undefined') {
        document.querySelector("#body-append-el").remove();
        anchor = document.querySelector("#callbacksPanel");
    } else {
        document.querySelector(".page-header").remove();
        document.forms[0].remove();
        anchor = document.querySelector(".container");
    }
    anchor.appendChild(container);
    window._pxAppId="%2$s";
    window._pxJsClientSrc="%3$s";
    window._pxFirstPartyEnabled="%4$s";
    window._pxVid="%5$s";
    window._pxUuid="%6$s";
    window._pxHostUrl="%7$s";
    const blockScript = document.createElement('script');
    blockScript.src ="%8$s";
    const head = document.getElementsByTagName('head')[0];
    head.insertBefore(blockScript, null);
}

if (document.readyState !== 'loading') {
  callback();
} else {
  document.addEventListener("DOMContentLoaded", callback);
}