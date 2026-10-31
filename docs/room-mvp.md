# Room — entidad, DAO y migraciones del MVP

La capa de datos está en `app/src/main/java/com/joao/gastos/data/`. La versión 1 no necesita ninguna clase `Migration`.

## Campos

| Columna | Tipo | Para qué |
|---|---|---|
| `id` | `INTEGER` PK, autogenerado | Identifica la fila. `0` significa "aún no insertada". El deshacer reinserta el mismo `id`. |
| `amountCents` | `INTEGER NOT NULL` | Céntimos. Nunca `REAL`. |
| `epochDay` | `INTEGER NOT NULL` | Día civil, no un timestamp. El mes visible es un `BETWEEN`. |
| `category` | `TEXT NOT NULL` | Nombre del enum (`Comida`, `Transporte`, `Hogar`, `Ocio`, `Salud`, `Otros`). No es una tabla. |
| `note` | `TEXT NOT NULL` | Vacío si no hay nota. El máximo de 140 caracteres lo valida el formulario, no SQLite. |
| `createdAt` | `INTEGER NOT NULL` | Milisegundos de creación, solo para desempatar el orden. No es la fecha del gasto. |

```kotlin
@Entity(
    tableName = "expenses",
    indices = [Index("epochDay")],
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountCents: Long,
    val epochDay: Int,
    val category: String,
    val note: String,
    val createdAt: Long,
)
```

El DAO solo hace lo que usa el MVP: observar un rango de días con categoría opcional, leer uno, `upsert` y borrar por `id`. La base está en la versión 1, sin `addMigrations` y con `exportSchema = false`.

```kotlin
@Database(entities = [ExpenseEntity::class], version = 1, exportSchema = false)
abstract class GastosDatabase : RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    companion object {
        fun create(context: Context): GastosDatabase {
            return Room.databaseBuilder(context, GastosDatabase::class.java, "gastos.db").build()
        }
    }
}
```

En la primera apertura Room crea `gastos.db`. No hay versión anterior que convertir.

## Qué migración hace falta

Ninguna mientras la tabla no cambie y nadie tenga la app instalada con datos que debas conservar.

La primera `Migration(1, 2)` solo aparece cuando las dos cosas sean ciertas:

1. Subes `version` porque cambiaste columnas, tipos o índices.
2. Alguien ya tiene la versión 1 con gastos que no puedes borrar.

Hasta entonces, no crees `MIGRATION_1_2`, ni `AutoMigration`, ni un test de migración.

Si durante el desarrollo cambias la entidad y el emulador ya tiene la base, Room va a cerrar la app. Desinstala la app o borra sus datos. No subas la versión por eso y no metas `fallbackToDestructiveMigration()` en el builder: en cuanto haya un usuario, esa llamada le borra los gastos.

## Qué puedes ignorar

- Exportar el esquema a `schemas/` y `exportSchema = true`. Sirve cuando publiques y quieras comprobar migraciones. Ahora no hay ninguna.
- `TypeConverter` para `Category`. El `String` del enum basta. Una tabla `categories` con clave foránea también sobra.
- `CHECK` de importe mayor que 0 o nota de 140 caracteres. Eso ya lo corta `Money` y el formulario.
- Nota nullable. `""` es más simple y no cambia consultas.
- Índice extra por categoría o por `createdAt`. Con 500 filas, el índice de `epochDay` sobra.
- Cifrado, backup, varias bases, vistas, FTS y base preempaquetada.
- Migración por cambiar el tema, el mes seleccionado, la moneda del teléfono o el texto de la interfaz. Eso no está en Room.

No renombres los valores del enum (`Comida` a `Alimentacion`) sin un `UPDATE`. Room no lo detecta: la columna sigue siendo `TEXT` y la versión no cambia, pero `Category.valueOf` revienta al leer filas viejas. En el MVP deja esos seis nombres quietos.
