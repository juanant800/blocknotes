# BlockNotes (MVP)
Editor de bloques estilo Notion, original. Kotlin + Jetpack Compose + Room.

## Ejecutar
1. Android Studio (Koala o más nuevo) → Open → esta carpeta. Deja que sincronice Gradle.
2. Run ▶ en un emulador/dispositivo (API 26+).

## Qué hay
- Páginas con subpáginas (parentId) · bloques: texto, H1, H2, tarea, lista, código
- Enter parte el bloque · Backspace en vacío lo borra · escribe "/" para cambiar de tipo
- Room local. Ids UUID + updatedAt + borrado lógico = listo para sync (last-write-wins)

## Siguiente
Reordenar bloques (drag), bases de datos (tabla/tablero), sync (Supabase/Firebase), búsqueda.
