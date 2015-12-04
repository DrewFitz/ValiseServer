var nodes = [];

function $id(id){
    return document.getElementById(id);
}
function DirParse (msg) {
    var marray = msg.split("!**");
    for(var i = 0; i < (marray.length-1); i++){
        DirOutput(marray[i]);
    }


}
function DirOutput(msg){
    var d = $id("dirprev");
    var marray = msg.split("/*");
    var duplicate = false;
    if(nodes.length != 0){
        for(var i = 0; i < nodes.length; i++){
            if(nodes[i] == marray[0]){
                duplicate = true;
                break;
            }

        }


    }
    if(duplicate == false){
        var child = document.createElement("item");
        child.className = "item";
        child.setAttribute("id", marray[0]);
        child.setAttribute("link", marray[1]);
        child.style.display = "default";
        var grandchild = document.createElement('a');
        grandchild.innerHTML = marray[0];
        grandchild.href = marray[1];
        child.appendChild(grandchild);
        d.appendChild(child);
        nodes.push(marray[0]);
    }



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
    xhr.open("PATCH", window.location.pathname, true);
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