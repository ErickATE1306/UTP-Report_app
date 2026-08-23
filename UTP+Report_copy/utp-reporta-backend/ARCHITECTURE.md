# Arquitectura del backend

El backend es un monolito modular: se despliega como una sola aplicación Spring Boot, pero cada dominio conserva su controller, DTO, mapper, modelo, repositorio y servicios.

```text
com.utp_reporta_backend/
├── common/
│   ├── config/
│   ├── exception/
│   ├── openapi/
│   └── security/
└── modules/
    └── <modulo>/
        ├── controller/
        ├── dto/
        │   ├── request/
        │   └── response/
        ├── mapper/
        ├── model/
        ├── repository/
        └── service/
            ├── <Modulo>Service.java
            ├── impl/
            ├── command/
            │   └── impl/
            └── query/
                └── impl/
```

Flujo: `Controller → Service (fachada) → Command/Query → Repository → MySQL`.

- `controller`: recibe HTTP y llama únicamente a la fachada.
- `dto/request` y `dto/response`: contratos de entrada y salida.
- `mapper`: conversión entre entidades y respuestas mediante MapStruct.
- `command`: operaciones que modifican estado.
- `query`: operaciones de lectura, marcadas como `readOnly`.
- `common`: configuración transversal sin lógica propia de un módulo.

Swagger UI: `/swagger-ui-custom.html`. La documentación está agrupada por autenticación, usuarios, catálogos y reportes, con autorización Bearer JWT.
