"use strict";
/*
 * ATTENTION: An "eval-source-map" devtool has been used.
 * This devtool is neither made for production nor for readable output files.
 * It uses "eval()" calls to create a separate source file with attached SourceMaps in the browser devtools.
 * If you are trying to read the output file, select a different devtool (https://webpack.js.org/configuration/devtool/)
 * or disable the default devtool with "devtool: false".
 * If you are looking for production-ready output files, see mode: "production" (https://webpack.js.org/configuration/mode/).
 */
exports.id = "vendor-chunks/hast-util-parse-selector@2.2.5";
exports.ids = ["vendor-chunks/hast-util-parse-selector@2.2.5"];
exports.modules = {

/***/ "(ssr)/../../node_modules/.pnpm/hast-util-parse-selector@2.2.5/node_modules/hast-util-parse-selector/index.js":
/*!**************************************************************************************************************!*\
  !*** ../../node_modules/.pnpm/hast-util-parse-selector@2.2.5/node_modules/hast-util-parse-selector/index.js ***!
  \**************************************************************************************************************/
/***/ ((module) => {

eval("\n\nmodule.exports = parse\n\nvar search = /[#.]/g\n\n// Create a hast element from a simple CSS selector.\nfunction parse(selector, defaultTagName) {\n  var value = selector || ''\n  var name = defaultTagName || 'div'\n  var props = {}\n  var start = 0\n  var subvalue\n  var previous\n  var match\n\n  while (start < value.length) {\n    search.lastIndex = start\n    match = search.exec(value)\n    subvalue = value.slice(start, match ? match.index : value.length)\n\n    if (subvalue) {\n      if (!previous) {\n        name = subvalue\n      } else if (previous === '#') {\n        props.id = subvalue\n      } else if (props.className) {\n        props.className.push(subvalue)\n      } else {\n        props.className = [subvalue]\n      }\n\n      start += subvalue.length\n    }\n\n    if (match) {\n      previous = match[0]\n      start++\n    }\n  }\n\n  return {type: 'element', tagName: name, properties: props, children: []}\n}\n//# sourceURL=[module]\n//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJmaWxlIjoiKHNzcikvLi4vLi4vbm9kZV9tb2R1bGVzLy5wbnBtL2hhc3QtdXRpbC1wYXJzZS1zZWxlY3RvckAyLjIuNS9ub2RlX21vZHVsZXMvaGFzdC11dGlsLXBhcnNlLXNlbGVjdG9yL2luZGV4LmpzIiwibWFwcGluZ3MiOiJBQUFZOztBQUVaOztBQUVBOztBQUVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7QUFDQTs7QUFFQTtBQUNBO0FBQ0E7QUFDQTs7QUFFQTtBQUNBO0FBQ0E7QUFDQSxRQUFRO0FBQ1I7QUFDQSxRQUFRO0FBQ1I7QUFDQSxRQUFRO0FBQ1I7QUFDQTs7QUFFQTtBQUNBOztBQUVBO0FBQ0E7QUFDQTtBQUNBO0FBQ0E7O0FBRUEsVUFBVTtBQUNWIiwic291cmNlcyI6WyJ3ZWJwYWNrOi8vd2ViLy4uLy4uL25vZGVfbW9kdWxlcy8ucG5wbS9oYXN0LXV0aWwtcGFyc2Utc2VsZWN0b3JAMi4yLjUvbm9kZV9tb2R1bGVzL2hhc3QtdXRpbC1wYXJzZS1zZWxlY3Rvci9pbmRleC5qcz8yYjhlIl0sInNvdXJjZXNDb250ZW50IjpbIid1c2Ugc3RyaWN0J1xuXG5tb2R1bGUuZXhwb3J0cyA9IHBhcnNlXG5cbnZhciBzZWFyY2ggPSAvWyMuXS9nXG5cbi8vIENyZWF0ZSBhIGhhc3QgZWxlbWVudCBmcm9tIGEgc2ltcGxlIENTUyBzZWxlY3Rvci5cbmZ1bmN0aW9uIHBhcnNlKHNlbGVjdG9yLCBkZWZhdWx0VGFnTmFtZSkge1xuICB2YXIgdmFsdWUgPSBzZWxlY3RvciB8fCAnJ1xuICB2YXIgbmFtZSA9IGRlZmF1bHRUYWdOYW1lIHx8ICdkaXYnXG4gIHZhciBwcm9wcyA9IHt9XG4gIHZhciBzdGFydCA9IDBcbiAgdmFyIHN1YnZhbHVlXG4gIHZhciBwcmV2aW91c1xuICB2YXIgbWF0Y2hcblxuICB3aGlsZSAoc3RhcnQgPCB2YWx1ZS5sZW5ndGgpIHtcbiAgICBzZWFyY2gubGFzdEluZGV4ID0gc3RhcnRcbiAgICBtYXRjaCA9IHNlYXJjaC5leGVjKHZhbHVlKVxuICAgIHN1YnZhbHVlID0gdmFsdWUuc2xpY2Uoc3RhcnQsIG1hdGNoID8gbWF0Y2guaW5kZXggOiB2YWx1ZS5sZW5ndGgpXG5cbiAgICBpZiAoc3VidmFsdWUpIHtcbiAgICAgIGlmICghcHJldmlvdXMpIHtcbiAgICAgICAgbmFtZSA9IHN1YnZhbHVlXG4gICAgICB9IGVsc2UgaWYgKHByZXZpb3VzID09PSAnIycpIHtcbiAgICAgICAgcHJvcHMuaWQgPSBzdWJ2YWx1ZVxuICAgICAgfSBlbHNlIGlmIChwcm9wcy5jbGFzc05hbWUpIHtcbiAgICAgICAgcHJvcHMuY2xhc3NOYW1lLnB1c2goc3VidmFsdWUpXG4gICAgICB9IGVsc2Uge1xuICAgICAgICBwcm9wcy5jbGFzc05hbWUgPSBbc3VidmFsdWVdXG4gICAgICB9XG5cbiAgICAgIHN0YXJ0ICs9IHN1YnZhbHVlLmxlbmd0aFxuICAgIH1cblxuICAgIGlmIChtYXRjaCkge1xuICAgICAgcHJldmlvdXMgPSBtYXRjaFswXVxuICAgICAgc3RhcnQrK1xuICAgIH1cbiAgfVxuXG4gIHJldHVybiB7dHlwZTogJ2VsZW1lbnQnLCB0YWdOYW1lOiBuYW1lLCBwcm9wZXJ0aWVzOiBwcm9wcywgY2hpbGRyZW46IFtdfVxufVxuIl0sIm5hbWVzIjpbXSwic291cmNlUm9vdCI6IiJ9\n//# sourceURL=webpack-internal:///(ssr)/../../node_modules/.pnpm/hast-util-parse-selector@2.2.5/node_modules/hast-util-parse-selector/index.js\n");

/***/ })

};
;