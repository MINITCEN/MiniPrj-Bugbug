const params = new URLSearchParams(window.location.search);
const error = params.get("error");

if (error === "forbidden") {
    alert("의뢰인만 의뢰를 등록/수정/삭제할 수 있습니다.");
}