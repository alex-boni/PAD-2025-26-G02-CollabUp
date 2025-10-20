Base de datos PAD

## Usuarios
--
## Collabs
--
### CollabView
(Guarda referencia a calendario, una lista etc, NO LOS DATOS -> eso CollabItem)
- Id obl
- Tipo (String) obl -> enumerado en código
- Ajustes (json?) -> tener como los ajustes de la lista o calendario (
- Items (array id items) -> lista de items en la view, aunque se repite la info se guarda este campo por complejidad.


*En el collab en si:
- Etiquetas disponibles (array de mapa nombre,color¿?)
- Orden de views 



##### Operaciones necesarias:
*Considerar tener el objeto Grupo como atributo en distintas clases de View donde nos haga falta. 
- addView(int idGrupo, ¿next id available?, string tipo, ¿ajustes?)
- getView(int idView) //necesitamos id! -> para hacer la lista de views que te sale nada más entrar al grupo
- edit(idgrupo, idview, ajustes) -> editar view ajustes
- getAllviews(int idGrupo) -> coge todas las cosas del grupo.
- delete(idgrupo, idview)

Operaciones a parte de base de datos

openView

getAllViews() -> saca vistas aplicación para +




### CollabItems
("Eventos")
- Id (int) obl
- Nombre (string) obl
- Descripción string
- Ubicacion? geoloc(no priori) 
- Fecha (timestamp) 
- Usuarios asignados (array de ints) -> usuarios a los que se le ha asignado el item
- Etiqueta (id int asociado a etiqueta) -> etiqueta asignada a el evento
- CollabViews asignadas (array de ids collabview )
- Datos json -> cositas especiales opcionales que hay que añadirle dependiendo de la view en la que esté 


La nota es un item con id, nombre y descripción.

Todo lo que vaya a calendario tiene fecha.

La lista es una lista de items separados. 


##### Operaciones necesarias:
- addItem(idgrupo, lista idviews, ...nombre fecha...)
- addItemToView(idgrupo, iditem, idview)
- deleteItemFromView(idgrupo,iditem, idview)
- getAll(idgrupo) -> por si hacemos lista donde se puedan ver todos

*En plan filtro:

Mapa (lo que quieres filtrar, valor que quieres que tenga)
- get(idgrupo) filtrando los que tienen fecha (para calendario probablemente) (fecha, not null) +-
- get(idgrupo) filtrando por usuario(s) (usr, nombre/id)
- get(idgrupo) filtrando por idview (usar campo view para no tener complejidad horrorosa)


edit(idgrupo, iditem) -> ojito cambiar también de la tabla view, si se han cambiado las views en las que está.
delete(idgrupo, iditem) -> ojito quitar tb de tabla view



