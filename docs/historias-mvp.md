# Historias de usuario — MVP gestor de gastos offline

Corte de un MVP offline para Android, no un backlog de producto. Seis historias, en orden de construcción. Si una no cabe en el primer release, no entra.

## Alcance del MVP

Una sola pantalla de lista, un formulario y persistencia local. Sin cuenta, sin red y sin permiso `INTERNET`. El dinero se guarda en céntimos (`Long`), nunca en `Float`/`Double`. La fecha es solo día local, no un timestamp con zona horaria.

## Historias

### 1. Registrar un gasto sin red

Como usuario, quiero anotar un gasto en pocos toques y que quede guardado aunque no haya conexión.

**Criterios de aceptación**

- El formulario pide importe, fecha, categoría y nota. La fecha por defecto es hoy. La nota es opcional, con un máximo de 140 caracteres.
- El importe es obligatorio, mayor que 0, con como máximo 2 decimales y un tope de 999.999,99. El teclado es numérico.
- La categoría es obligatoria y sale de una lista fija: Comida, Transporte, Hogar, Ocio, Salud, Otros.
- Guardar escribe en base de datos local y vuelve a la lista con el gasto visible. No hay llamada de red ni estado de "sincronizando".
- Funciona en modo avión. Matar el proceso después de guardar no pierde el gasto.
- Un importe vacío, 0 o con más de 2 decimales no se guarda y el error se muestra en el campo.

**Fuera:** foto del ticket, OCR, voz, ubicación, etiquetas, gasto recurrente y borrador si se cierra el formulario sin guardar.

### 2. Ver los gastos del mes

Como usuario, quiero abrir la app y ver los gastos del mes actual, para saber en qué se me fue el dinero.

**Criterios de aceptación**

- La lista abre directo, sin login ni onboarding. Por defecto muestra el mes actual, ordenada por fecha descendente y, a igual fecha, por creación descendente.
- Cada fila muestra importe formateado con la locale del dispositivo, categoría, fecha y nota solo si existe.
- Arriba se ve el total del mes visible. El total se recalcula tras alta, edición o borrado, y coincide con la suma de las filas.
- Mes anterior y mes siguiente cambian la lista y el total. No hay filtro "todos los tiempos".
- Lista vacía: mensaje claro y acción para añadir el primer gasto. Cero gastos no es un error.
- Reiniciar la app o el dispositivo conserva los datos. Rotar la pantalla no duplica filas ni pierde el mes seleccionado.
- 500 gastos en el mes se desplazan sin ANR.

**Fuera:** búsqueda por texto, gráficos, comparación entre meses, presupuestos y widgets.

### 3. Filtrar por categoría

Como usuario, quiero ver solo una categoría del mes visible, para revisar un tipo de gasto.

**Criterios de aceptación**

- Un selector con "Todas" más las 6 categorías fijas filtra la lista y el total del mes ya seleccionado.
- Combinar mes y categoría es acumulable. Cambiar de mes mantiene la categoría elegida.
- Estado vacío específico si ese mes no tiene gastos de esa categoría.
- Rotar o morir el proceso conserva mes y categoría seleccionados.

**Fuera:** crear, renombrar, borrar u ordenar categorías. Subcategorías. Varias categorías por gasto.

### 4. Corregir un gasto

Como usuario, quiero editar un gasto mal apuntado sin crear un duplicado.

**Criterios de aceptación**

- Abrir una fila carga el mismo formulario, con los valores actuales.
- Guardar actualiza esa fila y el total. No inserta otro registro.
- Valen las mismas reglas de importe, fecha y categoría que al crear.
- Cancelar descarta los cambios.
- Rotar a mitad de edición no pierde lo ya tecleado.

**Fuera:** historial de cambios, edición masiva y deshacer una edición.

### 5. Borrar un gasto

Como usuario, quiero eliminar un gasto duplicado o erróneo sin borrarlo por accidente.

**Criterios de aceptación**

- Borrar pide confirmación. Cancelar no cambia nada.
- Confirmar elimina solo ese gasto, actualiza lista y total, y sobrevive a reiniciar la app.
- Tras borrar, un snackbar permite deshacer durante unos segundos y restaura el mismo gasto, no una copia.
- El botón de borrar tiene área táctil de al menos 48 dp y nombre accesible para TalkBack.

**Fuera:** papelera, borrado masivo y borrado de todo un mes.

### 6. Sobrevivir al uso real del teléfono

Como usuario, quiero que la app no pierda datos ni se rompa con el uso normal de Android.

**Criterios de aceptación**

- No declara permiso `INTERNET`. El flujo principal no depende de red, cuenta de Google ni servicios de Google Play.
- Los datos viven en almacenamiento privado de la app. No se piden permisos de almacenamiento.
- El importe se persiste en unidades menores (`Long`). La UI formatea y parsea con la locale, sin `Double` para el dinero.
- La fecha guardada es un día civil local. Cambiar la zona horaria del dispositivo no mueve el gasto de mes.
- Proceso en segundo plano, rotación y cambio de tema claro/oscuro no corrompen la lista ni el formulario.
- Un gasto con nota larga no rompe la fila. Textos cortados siguen siendo legibles.

**Fuera:** backup en la nube, exportar CSV, importar del banco, bloqueo con biometría, cifrado extra y sincronización entre dispositivos.

## Qué no construir en este MVP

Déjalo fuera aunque parezca pequeño:

- Cuentas, login y multiusuario.
- Varias monedas o varias carteras.
- Presupuestos, alertas y gastos recurrentes.
- Gráficos, informes y exportar o compartir.
- Fotos, OCR y adjuntos.
- Categorías personalizadas.
- Cualquier sync, incluido "por si luego lo conectamos".

El primer release está cerrado cuando se puede anotar un gasto en modo avión, ver el total del mes, filtrar por categoría, corregir, borrar con deshacer, y seguir viendo los mismos datos después de matar la app.
