# Gestor de gastos

App Android offline para anotar gastos. No pide cuenta, no usa red y no declara el permiso `INTERNET`.

La versión actual es **0.3.2**, una pre-release para probar en un dispositivo. No está publicada en Play.

## Qué hace

- Registrar un gasto: importe, fecha, categoría fija y nota opcional.
- Ver los gastos del mes y el total.
- Filtrar por categoría.
- Corregir y borrar, con confirmación y deshacer.
- Conservar los datos en el teléfono después de cerrar la app.

El dinero se guarda en céntimos. La fecha es un día civil, no un timestamp.

## Requisitos

- JDK 17
- Android SDK 35
- `minSdk` 26

Android Studio crea `local.properties` con la ruta del SDK. Ese archivo no se versiona.

## Documentación

- [Interfaz Yesca](docs/yesca-interfaz.md)
- [Historias del MVP](docs/historias-mvp.md)
- [Estructura de paquetes](docs/estructura-paquetes.md)
- [Room y migraciones](docs/room-mvp.md)

## Compilar

```powershell
.\gradlew.bat assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`.

## Instalar la pre-release 0.3.2

1. Descarga `gestor-de-gastos-0.3.2-debug.apk` desde la pre-release `v0.3.2`.
2. En el teléfono, permite instalar apps de origen desconocido.
3. Abre el APK e instálalo.

Es un APK debug, firmado con la clave de depuración. Sirve para probar, no para Play. Si más adelante instalas un build firmado con otra clave, Android pedirá desinstalar esta copia. Desinstalar borra los gastos.

`0.3.2` deja el encabezado y el botón de registrar dentro del área útil de la pantalla, por debajo de la barra de notificaciones y por encima de la barra de navegación. Si ya tenías la 0.3.1, instala esta encima. La firma debug es la misma y los movimientos anteriores se conservan. No hay cambio de base de datos.

## Qué no incluye

Cuentas, sincronización, varias monedas, presupuestos, gráficos, fotos del ticket y categorías personalizadas.
