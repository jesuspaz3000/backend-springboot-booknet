# Endpoints de Recomendaciones y Destacados - BookNet API

## 1. GET /api/books/featured
**Descripción:** Obtiene los 10 libros destacados con mayor promedio de calificaciones.
**Autenticación:** No requerida (público)

### Ejemplo de Respuesta:
```json
{
  "success": true,
  "message": "Libros destacados obtenidos exitosamente",
  "data": [
    {
      "id": "book-id-1",
      "title": "El Señor de los Anillos",
      "isbn": "9780547928227",
      "description": "Una épica aventura en la Tierra Media...",
      "publicationYear": 1954,
      "pageCount": 1216,
      "language": "Español",
      "averageRating": 4.8,
      "ratingCount": 1250,
      "readingDifficulty": "Intermedio",
      "ageRating": "13+",
      "coverImageUrl": "https://example.com/cover1.jpg",
      "authors": [
        {
          "id": "author-1",
          "name": "J.R.R. Tolkien",
          "biography": "Escritor británico...",
          "birthDate": "1892-01-03",
          "nationality": "Británico"
        }
      ],
      "genres": [
        {
          "id": "genre-1",
          "name": "Fantasía",
          "description": "Literatura fantástica"
        }
      ],
      "tags": [
        {
          "id": "tag-1",
          "name": "Aventura",
          "description": "Libros de aventuras"
        }
      ]
    }
    // ... hasta 10 libros
  ]
}
```

## 2. GET /api/books/recommendations
**Descripción:** Obtiene recomendaciones personalizadas basadas en los géneros de libros bien calificados por el usuario (≥4.0 estrellas).
**Autenticación:** Requerida (JWT Token)
**Headers:** `Authorization: Bearer <jwt-token>`

### Ejemplo de Respuesta:
```json
{
  "success": true,
  "message": "Recomendaciones obtenidas exitosamente",
  "data": [
    {
      "id": "book-id-5",
      "title": "Dune",
      "isbn": "9780441172719",
      "description": "Una saga épica de ciencia ficción...",
      "publicationYear": 1965,
      "pageCount": 688,
      "language": "Español",
      "averageRating": 4.6,
      "ratingCount": 890,
      "readingDifficulty": "Avanzado",
      "ageRating": "16+",
      "coverImageUrl": "https://example.com/cover5.jpg",
      "authors": [
        {
          "id": "author-5",
          "name": "Frank Herbert",
          "biography": "Escritor estadounidense...",
          "birthDate": "1920-10-08",
          "nationality": "Estadounidense"
        }
      ],
      "genres": [
        {
          "id": "genre-2",
          "name": "Ciencia Ficción",
          "description": "Literatura de ciencia ficción"
        }
      ],
      "tags": [
        {
          "id": "tag-2",
          "name": "Épico",
          "description": "Historias épicas"
        }
      ]
    }
    // ... hasta 10 libros recomendados
  ]
}
```

## 3. GET /api/books/continue-reading
**Descripción:** Obtiene libros calificados por el usuario ordenados de mayor a menor puntuación (libros que más le gustaron).
**Autenticación:** Requerida (JWT Token)
**Headers:** `Authorization: Bearer <jwt-token>`

### Ejemplo de Respuesta:
```json
{
  "success": true,
  "message": "Libros para continuar leyendo obtenidos exitosamente",
  "data": [
    {
      "id": "book-id-10",
      "title": "1984",
      "isbn": "9780451524935",
      "description": "Una distopía sobre el totalitarismo...",
      "publicationYear": 1949,
      "pageCount": 328,
      "language": "Español",
      "averageRating": 4.7,
      "ratingCount": 2100,
      "readingDifficulty": "Intermedio",
      "ageRating": "16+",
      "coverImageUrl": "https://example.com/cover10.jpg",
      "authors": [
        {
          "id": "author-10",
          "name": "George Orwell",
          "biography": "Escritor británico...",
          "birthDate": "1903-06-25",
          "nationality": "Británico"
        }
      ],
      "genres": [
        {
          "id": "genre-3",
          "name": "Distopía",
          "description": "Literatura distópica"
        }
      ],
      "tags": [
        {
          "id": "tag-3",
          "name": "Clásico",
          "description": "Literatura clásica"
        }
      ]
    }
    // ... libros ordenados por la calificación que les dio el usuario (de mayor a menor)
  ]
}
```

## 4. GET /api/books/top10
**Descripción:** Obtiene top 10 libros con mayor promedio de calificaciones. Si hay menos de 10 libros calificados, completa con libros relacionados por género y luego con libros recientes.
**Autenticación:** No requerida (público)

### Ejemplo de Respuesta:
```json
{
  "success": true,
  "message": "Top 10 libros obtenidos exitosamente",
  "data": [
    {
      "id": "book-id-15",
      "title": "Cien Años de Soledad",
      "isbn": "9780307474728",
      "description": "Una obra maestra del realismo mágico...",
      "publicationYear": 1967,
      "pageCount": 417,
      "language": "Español",
      "averageRating": 4.9,
      "ratingCount": 1800,
      "readingDifficulty": "Avanzado",
      "ageRating": "18+",
      "coverImageUrl": "https://example.com/cover15.jpg",
      "authors": [
        {
          "id": "author-15",
          "name": "Gabriel García Márquez",
          "biography": "Escritor colombiano...",
          "birthDate": "1927-03-06",
          "nationality": "Colombiano"
        }
      ],
      "genres": [
        {
          "id": "genre-4",
          "name": "Realismo Mágico",
          "description": "Literatura de realismo mágico"
        }
      ],
      "tags": [
        {
          "id": "tag-4",
          "name": "Premio Nobel",
          "description": "Obras ganadoras del Premio Nobel"
        }
      ]
    }
    // ... hasta 10 libros (combinando mejor calificados + relacionados + recientes si es necesario)
  ]
}
```

## Estructura del BookResponse

Todos los endpoints devuelven libros con la siguiente estructura completa:

```json
{
  "id": "string",                    // ID único del libro
  "title": "string",                 // Título del libro
  "isbn": "string",                  // ISBN del libro
  "description": "string",           // Descripción del libro
  "publicationYear": "integer",      // Año de publicación
  "pageCount": "integer",            // Número de páginas
  "language": "string",              // Idioma del libro
  "averageRating": "double",         // Promedio de calificaciones (0.0-5.0)
  "ratingCount": "integer",          // Número total de calificaciones
  "readingDifficulty": "string",     // Dificultad de lectura
  "ageRating": "string",             // Clasificación por edad
  "coverImageUrl": "string",         // URL de la imagen de portada
  "authors": [                       // Array de autores
    {
      "id": "string",
      "name": "string",
      "biography": "string",
      "birthDate": "string",
      "nationality": "string"
    }
  ],
  "genres": [                        // Array de géneros
    {
      "id": "string",
      "name": "string",
      "description": "string"
    }
  ],
  "tags": [                          // Array de tags
    {
      "id": "string",
      "name": "string",
      "description": "string"
    }
  ]
}
```

## Lógica de Relleno Inteligente (Top 10)

El endpoint `/api/books/top10` implementa una lógica de "relleno inteligente":

1. **Paso 1:** Obtiene libros con calificaciones ordenados por promedio descendente
2. **Paso 2:** Si faltan para 10, busca libros relacionados por género
3. **Paso 3:** Si aún faltan, completa con libros más recientes

## Códigos de Error

Todos los endpoints pueden devolver los siguientes errores:

### 401 Unauthorized (solo endpoints autenticados)
```json
{
  "success": false,
  "message": "Token de autenticación requerido",
  "data": null
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "Error al obtener [tipo de libros]: [mensaje específico]",
  "data": null
}
```

## Notas de Implementación

- Los endpoints utilizan `Neo4jClient` para consultas Cypher directas
- Las recomendaciones se basan en géneros de libros calificados con ≥4.0 estrellas
- Los libros se devuelven con todas sus relaciones (autores, géneros, tags) cargadas
- La paginación no es necesaria ya que todos los endpoints devuelven máximo 10 resultados
- Los endpoints públicos no requieren autenticación
- Los endpoints personalizados requieren JWT token válido
