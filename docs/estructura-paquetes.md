# Estructura de paquetes — Compose + Room + MVVM

Para este MVP basta un solo módulo `app` y unos 15 archivos. Dos pantallas, un repositorio y un contenedor manual. Sin capa domain, sin use cases y sin interfaces "por si acaso". Sin Hilt.

```text
app/src/main/java/com/joao/gastos/
├── GastosApp.kt                 # Application. Crea el contenedor
├── MainActivity.kt              # setContent y pasa el repositorio
├── di/
│   └── AppContainer.kt          # db + dao + repository. Solo eso
├── data/
│   ├── ExpenseEntity.kt         # tabla + toModel() / toEntity()
│   ├── ExpenseDao.kt
│   ├── GastosDatabase.kt
│   └── ExpenseRepository.kt     # única puerta a Room
├── model/
│   ├── Expense.kt               # id, cents, epochDay, category, note, createdAt
│   ├── Category.kt              # enum fijo de 6
│   └── Money.kt                 # value class sobre Long
└── ui/
    ├── theme/                   # Color, Type, Theme. Los 3 de plantilla
    ├── GastosAppRoot.kt         # NavHost: lista y formulario
    ├── list/
    │   ├── ExpenseListScreen.kt
    │   ├── ExpenseListViewModel.kt
    │   └── ExpenseListUiState.kt
    └── form/
        ├── ExpenseFormScreen.kt
        ├── ExpenseFormViewModel.kt
        └── ExpenseFormUiState.kt
```

El paquete base `com.joao.gastos` es un ejemplo. Cámbialo si ya tienes `applicationId`.

## Quién habla con quién

La UI no ve `ExpenseEntity` ni el DAO. El ViewModel no abre la base de datos. Room no formatea importes.

```text
Screen → ViewModel → ExpenseRepository → ExpenseDao
                ↘ Expense, Money, Category
```

`AppContainer` se construye en `Application.onCreate()` y la Activity entrega el repositorio al `NavHost`. Cada destino crea su ViewModel con `viewModelFactory { initializer { ... } }`. No hace falta `CompositionLocal`, ni un grafo, ni un `ViewModelProvider.Factory` genérico.

## Qué va en cada sitio

- **model.** Tipos que sobreviven a un cambio de tabla. `Money` guarda céntimos y parsea o formatea con la locale. `Category` es el enum, no una tabla.
- **data.** `ExpenseEntity` guarda `amountCents: Long` y `epochDay: Int`. El mes visible es un `BETWEEN` sobre ese día. El mapeo son dos funciones en el mismo archivo, no una clase `Mapper`.
- **ExpenseRepository.** `observe(month, category)`, `get(id)`, `upsert` y `delete`. El total del mes se calcula en el ViewModel sobre la lista ya filtrada. Con 500 filas no hace falta otra query.
- **list.** Mes, categoría, filas, total y el gasto recién borrado para el deshacer. El snackbar restaura esa misma instancia con `upsert`.
- **form.** Un destino, dos modos: sin id crea, con id edita. El estado guarda el texto del importe, no un `Double`. Validar al guardar. Rotar no pierde el texto porque vive en el ViewModel.

## Qué no crees

- `domain/`, `usecase/`, `datasource/` y un repositorio interface más una impl.
- Módulos Gradle `core`, `data` y `feature`.
- Paquetes `mapper`, `util`, `common` o `base`.
- Un `UiEvent` sellado por cada clic. El ViewModel expone funciones.
- Pantallas extra: ajustes, categorías, detalle. La fila abre el formulario.
- Hilt, Koin o un service locator. Si mañana entra Hilt, el único archivo que se sustituye es `AppContainer`.
