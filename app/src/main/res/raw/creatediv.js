function createDiv(id, parent)
{
	var elem = document.createElement('flex-item');
	elem.id = id;
	document.getElementById(parent).appendChild(elem);
	document.body.appendChild(elem);
	
}