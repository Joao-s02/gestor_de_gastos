# Interfaz Yesca

Propuesta aplicada sobre la app actual (Kotlin, Jetpack Compose, Material 3). El alcance no cambia: un mes, movimientos, alta, edición y borrado con confirmación. No hay cuentas, perfiles ni saludo.

Los ejemplos de este documento usan «Bs» solo como dato ficticio. La app sigue formateando con la moneda de la locale.

## Flujo

1. La app abre el mes actual. El isotipo identifica la marca; el mes es el título. Anterior y siguiente cambian de mes. Tocar el mes abre año y mes.
2. **Registrar movimiento** abre el formulario vacío, con el importe enfocado. **Guardar** vuelve al mes y confirma «Movimiento guardado». **Cancelar** descarta.
3. Tocar una fila abre **Editar movimiento** con los mismos campos. Guardar actualiza esa fila, no crea otra.
4. Deslizar una fila hacia la izquierda muestra **Borrar** y pide confirmación. Cancelar no cambia nada. Confirmar elimina solo ese movimiento.

## Pantallas

### Resumen mensual

Claro: fondo marfil. El isotipo original, completo, acompaña al mes con 4 dp de aire (el 10 % de una baldosa de 40 dp). El balance va en una tarjeta salvia de radio 24 dp, texto bosque. Debajo, ingresos y gastos con signo, icono y color. La lista muestra categoría, tipo, fecha, nota si existe e importe alineado a la derecha. El botón inferior es **Registrar movimiento**.

Oscuro: fondo bosque. La tarjeta de balance usa superficie oscura, no salvia, para que el texto marfil siga siendo legible. El botón pasa a marfil con texto bosque, como en el ejemplo del manual.

Estados: carga («Cargando movimientos»), vacío («Este mes no tiene movimientos.») y error con **Reintentar**. Ninguno juzga el saldo.

### Registro y edición

Selector **Gasto** / **Ingreso** con icono, palabra y fondo salvia si está elegido. El importe usa Sora 32 sp, peso 400. Las categorías llevan icono; la elegida se llena de salvia y pasa a peso 600. La fila **Fecha** abre el calendario. La nota es opcional, con contador. **Guardar** y **Cancelar** quedan al alcance con el teclado abierto porque el formulario se desplaza.

Validación: el error queda en el campo, en arcilla, sin disculpas. Mientras guarda, el botón dice **Guardando** y no se puede pulsar otra vez.

### Complementos

El selector de mes y año usa celdas de al menos 48 dp. El mes elegido tiene fondo salvia y peso 600, no solo color. El diálogo de borrado nombra la acción y dice que no se puede deshacer. El aviso de guardado repite el verbo: guardar produce «Movimiento guardado».

## Componentes

| Componente | Uso | Claro | Oscuro |
| --- | --- | --- | --- |
| Marca | Baldosa original de 40 dp, sin recorte ni nombre dentro | `icono.png` | La misma baldosa. No hay variante marfil en el archivo entregado |
| Tarjeta de balance | Radio 24 dp, padding 20 dp | Salvia / texto bosque | Superficie oscura / texto marfil |
| Fila de movimiento | Margen 16 dp, alto mínimo 64 dp, pauta inferior | Fondo marfil | Fondo bosque |
| Botón principal | Ancho completo, radio 24 dp, alto mínimo 48 dp, Sora 600 | Bosque / marfil | Marfil / bosque |
| Elección | Radio 24 dp, alto mínimo 48 dp | Salvia si está elegida | Salvia si está elegida, texto bosque |
| Campo | Línea inferior, sin caja | Pauta; error en arcilla | Pauta oscura; error en arcilla clara |
| Diálogo | Confirmación de borrado | Texto bosque, acción arcilla | Texto marfil, acción arcilla clara |

## Color

Institucionales, sin alterar:

| Nombre | Hex | Uso |
| --- | --- | --- |
| Bosque | `#153E36` | Texto y acción en claro. Fondo en oscuro |
| Salvia | `#8AB9A0` | Tarjeta de balance en claro, selección |
| Marfil | `#F3F5EE` | Fondo en claro. Texto y botón en oscuro |

Contraste de los pares institucionales: bosque/marfil 10,77:1, salvia/bosque 5,37:1. Marfil sobre salvia queda en 2,01:1 y no se usa.

Adaptaciones de interfaz, no de marca:

| Nombre | Hex | Para qué | Contraste |
| --- | --- | --- | --- |
| Bosque suave | `#45685E` | Texto secundario sobre marfil | 5,63:1 |
| Pauta | `#5E7A70` | Divisores sobre marfil | 4,25:1, por encima de 3:1 |
| Superficie oscura | `#1E564C` | Tarjeta sobre bosque | Marfil encima, 7,67:1 |
| Pauta oscura | `#528C7C` | Divisores sobre bosque | 3,04:1 |
| Hoja | `#146044` | Importe de ingreso en claro | 6,86:1 sobre marfil |
| Arcilla | `#7C3A32` | Gasto, error y borrar en claro | 7,62:1 sobre marfil |
| Arcilla clara | `#E7B2A8` | Gasto, error y borrar en oscuro | 6,39:1 sobre bosque |

Ingreso en oscuro usa salvia sobre bosque (5,37:1). Un ingreso no se distingue solo por el color: lleva signo +, icono y la palabra Ingreso. Un gasto lleva −, icono y la palabra Gasto.

## Tipografía

Sora, archivos reales de peso 400 y 600. No se simula la negrita.

| Rol | Peso | Tamaño | Interlineado |
| --- | --- | --- | --- |
| Títulos | 600 | 28 sp | 34 sp, cerca de 1,2 |
| Balance | 400 | 32 sp | 38 sp, cifras tabulares |
| Texto | 400 | 16 sp | 24 sp, 1,5 |
| Acciones | 600 | 16 sp | 21 sp, cerca de 1,3 |
| Importes de fila | 400 | 16 sp | Alineados a la derecha, sin recorte |

Si faltara el archivo, la contingencia del manual es la sans serif del sistema. Aquí el archivo sí está incluido.

## Espaciado y estado

Margen base 16 dp. Radios 16 dp en elecciones compactas y 24 dp en tarjeta y botón principal. Área táctil mínima 48 × 48 dp. El texto puede crecer: los controles usan alto mínimo, no alto fijo. El foco visible lo aporta el componente de Material. No hay animación de entrada.

## Identidad gráfica

El lanzador y la cabecera usan `icono.png` (1254 × 1254) sin recorte, sin reescalado hacia arriba y sin redibujo. El nombre de la app es Yesca y no va dentro de la baldosa. Entre el isotipo y el mes hay 8 dp, más que el 10 % de la baldosa de 40 dp.

Pendiente, y no sustituido:

- Máster vectorial.
- Variante clara sobre marfil. El archivo entregado es la baldosa oscura completa.
- Exportación específica de tienda distinta de este PNG. Android aplicará su máscara de plataforma sobre el archivo completo.

## Decisiones

El ejemplo del manual saluda a una persona y no muestra el selector de mes ni los totales. Aquí el mes sustituye al saludo, porque la app no tiene perfiles, y los totales quedan fuera de la tarjeta salvia: hoja y arcilla no alcanzan 4,5:1 sobre salvia. En oscuro, la tarjeta no se rellena de salvia por la misma razón.

La interfaz anterior en teal y la prueba de libreta de tinta quedan reemplazadas por esta paleta.
