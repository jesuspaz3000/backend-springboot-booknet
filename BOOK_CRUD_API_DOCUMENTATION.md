# Documentación de API - CRUD de Libros

Esta documentación describe todos los endpoints disponibles para la gestión de libros en el backend de BookNet.

## Base URL
```
http://localhost:8080/api/books
```

## Autenticación
Los endpoints marcados con 🔒 requieren autenticación Bearer Token.
Los endpoints marcados con 👑 requieren rol ADMIN.

---

## 1. Crear Libro Individual

**POST** `/api/books` 🔒👑

Crea un nuevo libro individual.

### Request Body
```json
{
  "title": "El Quijote",
  "isbn": "978-84-376-0494-7",
  "description": "Las aventuras del ingenioso hidalgo Don Quijote de la Mancha",
  "publicationYear": 1605,
  "pageCount": 863,
  "language": "español",
  "coverImage": "https://example.com/quijote-cover.jpg",
  "ageRating": "12+",
  "readingDifficulty": "avanzado",
  "authorIds": ["author-id-1", "author-id-2"],
  "genreIds": ["genre-id-1"],
  "tagIds": ["tag-id-1", "tag-id-2"],
  "seriesId": "series-id-1",
  "orderInSeries": 1
}
```

### Response (201 Created)
```json
{
  "success": true,
  "message": "Libro creado exitosamente",
  "data": {
    "id": "book-id-123",
    "title": "El Quijote",
    "isbn": "978-84-376-0494-7",
    "description": "Las aventuras del ingenioso hidalgo Don Quijote de la Mancha",
    "publicationYear": 1605,
    "pageCount": 863,
    "language": "español",
    "coverImage": "https://example.com/quijote-cover.jpg",
    "ageRating": "12+",
    "averageRating": 0.0,
    "totalRatings": 0,
    "readingDifficulty": "avanzado",
    "createdAt": "2024-01-15T10:30:00Z",
    "authors": [...],
    "genres": [...],
    "tags": [...],
    "series": {...},
    "totalChapters": 0
  },
  "timestamp": 1642248600000
}
```

---

## 2. Carga Masiva de Libros

**POST** `/api/books/bulk` 🔒👑

Crea múltiples libros en una sola operación (máximo 1000 libros).

### Request Body
```json
[
  {
    "title": "Libro 1",
    "isbn": "978-1234567890",
    "description": "Descripción del libro 1",
    "publicationYear": 2023,
    "pageCount": 300,
    "language": "español",
    "coverImage": "https://example.com/book1.jpg",
    "ageRating": "16+",
    "averageRating": 4.5,
    "totalRatings": 150,
    "readingDifficulty": "intermedio",
    "authorNames": ["Autor Uno", "Autor Dos"],
    "genreNames": ["Ficción", "Drama"],
    "tagNames": ["Contemporáneo", "Bestseller"],
    "seriesName": "Serie Ejemplo",
    "orderInSeries": 1
  },
  {
    "title": "Libro 2",
    // ... más libros
  }
]
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Libros creados exitosamente en lote",
  "data": {
    "booksCreated": 2,
    "totalRequested": 2,
    "books": [...]
  },
  "timestamp": 1642248600000
}
```

---

## 3. Obtener Libro por ID

**GET** `/api/books/{id}`

Obtiene un libro específico por su ID.

### Response (200 OK)
```json
{
  "success": true,
  "message": "Libro encontrado",
  "data": {
    "id": "book-id-123",
    "title": "El Quijote",
    // ... resto de campos del libro
  },
  "timestamp": 1642248600000
}
```

### Response (404 Not Found)
```json
{
  "success": false,
  "message": "Libro no encontrado",
  "timestamp": 1642248600000
}
```

---

## 4. Listar Libros con Paginación

**GET** `/api/books`

Obtiene una lista paginada de libros con ordenamiento.

### Query Parameters
- `page` (integer, default: 0): Número de página
- `size` (integer, default: 20, max: 100): Tamaño de página
- `sortBy` (string, default: "title"): Campo de ordenamiento (title, publicationYear, averageRating, createdAt)
- `sortDir` (string, default: "asc"): Dirección (asc, desc)

### Example Request
```
GET /api/books?page=0&size=10&sortBy=averageRating&sortDir=desc
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Libros obtenidos exitosamente",
  "data": {
    "books": [...],
    "currentPage": 0,
    "totalPages": 25,
    "totalElements": 500,
    "size": 10,
    "hasNext": true,
    "hasPrevious": false
  },
  "timestamp": 1642248600000
}
```

---

## 5. Actualizar Libro Completo

**PUT** `/api/books/{id}` 🔒👑

Actualiza todos los campos de un libro existente.

### Request Body
```json
{
  "title": "El Quijote - Edición Actualizada",
  "isbn": "978-84-376-0494-7",
  "description": "Nueva descripción",
  "publicationYear": 1605,
  "pageCount": 863,
  "language": "español",
  "coverImage": "https://example.com/new-cover.jpg",
  "ageRating": "12+",
  "readingDifficulty": "avanzado",
  "authorIds": ["author-id-1"],
  "genreIds": ["genre-id-1", "genre-id-2"],
  "tagIds": ["tag-id-1"],
  "seriesId": "series-id-1",
  "orderInSeries": 1
}
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Libro actualizado exitosamente",
  "data": {
    // libro actualizado
  },
  "timestamp": 1642248600000
}
```

---

## 6. Actualizar Libro Parcialmente

**PATCH** `/api/books/{id}` 🔒👑

Actualiza solo los campos especificados de un libro.

### Request Body (solo los campos a actualizar)
```json
{
  "title": "Nuevo título",
  "coverImage": "https://example.com/new-cover.jpg",
  "averageRating": 4.8,
  "totalRatings": 200
}
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Libro actualizado exitosamente",
  "data": {
    // libro actualizado
  },
  "timestamp": 1642248600000
}
```

---

## 7. Actualizar Solo Imagen de Portada

**PATCH** `/api/books/{id}/cover-image` 🔒👑

Actualiza únicamente la imagen de portada de un libro.

### Request Body
```json
{
  "coverImage": "https://example.com/nueva-portada.jpg"
}
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Imagen de portada actualizada exitosamente",
  "data": {
    // libro con imagen actualizada
  },
  "timestamp": 1642248600000
}
```

---

## 8. Eliminar Libro

**DELETE** `/api/books/{id}` 🔒👑

Elimina un libro del sistema.

### Response (200 OK)
```json
{
  "success": true,
  "message": "Libro eliminado exitosamente",
  "timestamp": 1642248600000
}
```

### Response (404 Not Found)
```json
{
  "success": false,
  "message": "Libro no encontrado",
  "timestamp": 1642248600000
}
```

---

## 9. Buscar Libros

**GET** `/api/books/search`

Busca libros por título, autor o género.

### Query Parameters
- `q` (string, required): Término de búsqueda (mínimo 2 caracteres)
- `page` (integer, default: 0): Número de página
- `size` (integer, default: 20, max: 100): Tamaño de página

### Example Request
```
GET /api/books/search?q=quijote&page=0&size=10
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Búsqueda completada exitosamente",
  "data": {
    "books": [...],
    "currentPage": 0,
    "totalPages": 3,
    "totalElements": 25,
    "size": 10,
    "hasNext": true,
    "hasPrevious": false,
    "searchTerm": "quijote"
  },
  "timestamp": 1642248600000
}
```

---

## 10. Obtener Libros por Género

**GET** `/api/books/genre/{genreName}`

Obtiene libros filtrados por género.

### Query Parameters
- `page` (integer, default: 0): Número de página
- `size` (integer, default: 20, max: 100): Tamaño de página

### Example Request
```
GET /api/books/genre/ficción?page=0&size=10
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Libros por género obtenidos exitosamente",
  "data": {
    "books": [...],
    "currentPage": 0,
    "totalPages": 15,
    "totalElements": 150,
    "size": 10,
    "hasNext": true,
    "hasPrevious": false,
    "genreName": "ficción"
  },
  "timestamp": 1642248600000
}
```

---

## 11. Obtener Libros por Autor

**GET** `/api/books/author/{authorName}`

Obtiene libros filtrados por autor.

### Query Parameters
- `page` (integer, default: 0): Número de página
- `size` (integer, default: 20, max: 100): Tamaño de página

### Example Request
```
GET /api/books/author/Miguel%20de%20Cervantes?page=0&size=10
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Libros por autor obtenidos exitosamente",
  "data": {
    "books": [...],
    "currentPage": 0,
    "totalPages": 2,
    "totalElements": 15,
    "size": 10,
    "hasNext": true,
    "hasPrevious": false,
    "authorName": "Miguel de Cervantes"
  },
  "timestamp": 1642248600000
}
```

---

## 12. Obtener Libros Mejor Calificados

**GET** `/api/books/top-rated`

Obtiene los libros con mejor calificación.

### Query Parameters
- `limit` (integer, default: 10, max: 100): Número de libros a retornar

### Example Request
```
GET /api/books/top-rated?limit=20
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Libros mejor calificados obtenidos exitosamente",
  "data": [
    {
      "id": "book-id-1",
      "title": "Libro Excelente",
      "averageRating": 4.9,
      "totalRatings": 1500,
      // ... resto de campos
    }
  ],
  "timestamp": 1642248600000
}
```

---

## 13. Obtener Libros Recientes

**GET** `/api/books/recent`

Obtiene los libros agregados más recientemente.

### Query Parameters
- `limit` (integer, default: 10, max: 100): Número de libros a retornar

### Example Request
```
GET /api/books/recent?limit=15
```

### Response (200 OK)
```json
{
  "success": true,
  "message": "Libros recientes obtenidos exitosamente",
  "data": [
    {
      "id": "book-id-1",
      "title": "Libro Nuevo",
      "createdAt": "2024-01-15T10:30:00Z",
      // ... resto de campos
    }
  ],
  "timestamp": 1642248600000
}
```

---

## Códigos de Error Comunes

### 400 Bad Request
- Datos de entrada inválidos
- Parámetros faltantes o incorrectos
- Formato de ISBN inválido
- Validaciones de negocio fallidas

### 401 Unauthorized
- Token de autenticación faltante o inválido

### 403 Forbidden
- Usuario sin permisos de ADMIN para operaciones de escritura

### 404 Not Found
- Libro no encontrado
- Recurso relacionado no encontrado

### 409 Conflict
- ISBN duplicado
- Conflicto de datos únicos

### 500 Internal Server Error
- Error interno del servidor
- Error de base de datos

---

## Notas Importantes

1. **Autenticación**: Todos los endpoints de escritura (POST, PUT, PATCH, DELETE) requieren autenticación Bearer Token y rol ADMIN.

2. **Paginación**: Los endpoints que retornan listas incluyen información de paginación completa.

3. **Validaciones**: 
   - ISBN debe ser válido (10 o 13 dígitos)
   - Títulos no pueden estar vacíos
   - Calificaciones deben estar entre 0.0 y 5.0
   - Al menos un autor es requerido

4. **Carga Masiva**: 
   - Máximo 1000 libros por operación
   - Crea automáticamente autores, géneros, etiquetas y series si no existen
   - Continúa procesando otros libros si uno falla

5. **CORS**: Configurado para permitir requests desde `http://localhost:3000`

6. **Ordenamiento**: Los campos válidos para ordenar son: `title`, `publicationYear`, `averageRating`, `createdAt`
