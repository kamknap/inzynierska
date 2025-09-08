// document.addEventListener("DOMContentLoaded", function () {
//   const svg = document.getElementById("svg1");

//   // Nasłuchuj kliknięcia na dowolną ścieżkę w SVG
//   svg.addEventListener("click", function (event) {
//     const target = event.target;
//     if (
//       (target.tagName === "path" || target.tagName === "polygon") &&
//       target.id
//     ) {
//       console.log("Kliknięto ścieżkę o id:", target.id);
//     }
//   });
// });

function sendIdToApp(id) {
  if (window.AndroidInterface && window.AndroidInterface.onPathClicked) {
    window.AndroidInterface.onPathClicked(id);
  }
}

document.addEventListener("DOMContentLoaded", function () {
  document.querySelectorAll(".muscle").forEach((el) => {
    el.addEventListener("click", () => {
      sendIdToApp(el.id);
    });
  });
});
