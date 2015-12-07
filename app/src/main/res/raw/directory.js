function $id(id){
    return document.getElementById(id);
}
function DirParse (msg) {
    var obj = JSON.parse(msg);
    var d = $id("dirprev");
    var child = d.firstChild;
    while( child ){
        d.removeChild(child);
        child = d.firstChild;
    }
    for (var i = 0, f; f = obj[i]; i++){
        DirOutput(f);
    }


}
function DirOutput(msg){
    var d = $id("dirprev");

    var child = document.createElement("item");
    child.className = "item";
    child.setAttribute("id", msg.filepath);
    child.setAttribute("link", msg.url);
    child.style.display = "default";
    var grandchild = document.createElement('a');
    grandchild.innerHTML = msg.name;
    grandchild.href = "/" + msg.url;
    child.appendChild(grandchild);

    var gcdelete = document.createElement('a');
    gcdelete.innerHTML = "delete";
    gcdelete.href = "#";
    gcdelete.style.padding="5px";
    gcdelete.onclick = function(){DeleteFile(msg.url);};
    gcdelete.className = "delete";


    var gcrename = document.createElement('a');
    gcrename.innerHTML = "rename";
    gcrename.href = "#";
    gcrename.style.padding="5px";
    gcrename.onclick = function(){RenameFile(msg.url);};
    gcrename.className = "rename";


    child.appendChild(gcdelete);

    child.appendChild(gcrename);

    d.appendChild(child);
    


}
window.onload = function init(){

    RefreshListHandler();


    refreshbutton.addEventListener("click", RefreshListHandler, false);

    var xhr = new XMLHttpRequest();
    if(xhr.upload) {
        document.addEventListener("dragover", FileDrag, false );
        document.addEventListener("dragleave", FileDrag, false );
        document.addEventListener("drop", FileSelect, false );

    }

}
function RefreshListHandler(e){

    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if(xhr.readyState == 4 && xhr.status == 200){
            DirParse(xhr.responseText);
        }
    };
    xhr.open("GET", window.location.pathname + ".json", true);
    xhr.send();

    }

function FileDrag(e){

    e.stopPropagation();
    e.preventDefault();

}
function FileSelect(e){
    FileDrag(e);
    var files = e.target.files || e.dataTransfer.files;
    for (var i = 0, f; f = files[i]; i++){
        UploadFile(f);
    }
}
function UploadFile(file){

    var form = $id("upload");
    var formData = new FormData();
    formData.append("file",file);

    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        if(xhr.readyState == 4 && xhr.status == 200){
            RefreshListHandler();
        }
    };
    if(xhr.upload){
        xhr.open("POST", window.location.pathname);
        xhr.send(formData);
    }
}
function DeleteFile(target){

    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function(){
        RefreshListHandler();
    }
    xhr.open("DELETE", "/"+target);
    xhr.send();
}
function RenameFile(target){
    var newname = prompt("Please enter the new filename", "");
    if(newname!= null){
        var resc = target + "/" + newname;
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function(){
                RefreshListHandler();
            }
        xhr.open("put", "/"+resc);
        xhr.send();
    }


}