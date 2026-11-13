# Fit Yourself documentación técnica y manual de usuario

Documento de referencia del proyecto Android Fit Yourself. Describe qué hace la aplicación, cómo está organizado su código, cómo se almacenan los datos, cómo se usa cada pantalla y cuáles son las limitaciones observables en la versión actual. Basado en el repositorio revisado el 13 de noviembre de 2026.

## 1 Resumen del proyecto

Fit Yourself es una aplicación móvil de registro de actividad física escrita en Kotlin para Android. Una persona puede crear una cuenta local, iniciar sesión, registrar una rutina con un ejercicio, peso, repeticiones y duración, añadir una foto y una ubicación opcionales y consultar los entrenamientos recientes. La interfaz también incluye una pantalla de perfil y una clasificación de usuarios.

La información se almacena en el teléfono mediante Room sobre SQLite. No hay servicio remoto, API propia, sincronización entre dispositivos ni panel web. La aplicación carga el estilo del mapa desde un servicio externo, por lo que esa parte sí depende de la conexión a internet.

El flujo principal implementado es: Registro de usuario → Inicio de sesión → Pantalla principal → Registro de entrenamiento → Historial en Inicio. En cada guardado se crea un entrenamiento y una sola serie asociada.

### Funciones presentes

- Registro de cuentas con nombre, correo y contraseña en la base local.
- Inicio de sesión mediante búsqueda por correo y comparación de contraseña.
- Navegación inferior entre Inicio, Entreno, Leaderboard y Perfil.
- Registro de rutina, ejercicio, peso, repeticiones, duración y texto de ubicación.
- Captura opcional de foto con la cámara y FileProvider.
- Mapa interactivo para seleccionar un punto y botón para centrar la última ubicación GPS conocida.
- Lista de entrenamientos con fecha, duración y foto, si existe.
- Perfil con nombre, correo y cantidad de entrenamientos.
- Clasificación ordenada por cantidad de fechas distintas con entrenamiento.
- Eliminación de entrenamientos con más de 24 horas cuando se abre o reanuda Inicio.

### Alcance real

El repositorio incluye una entidad y un DAO de ejercicios, pero el formulario no consulta ese catálogo: el nombre del ejercicio se escribe libremente. Tampoco hay edición o eliminación manual de entrenamientos, cierre de sesión, recuperación de contraseña, gestión de varias series por rutina, estadísticas históricas ni cálculo real de días consecutivos.

## 2 Requisitos y ejecución

El proyecto contiene un módulo Android llamado app. Se abre desde la carpeta raíz D:\Appfitness en Android Studio. El archivo settings.gradle.kts define el proyecto y agrega el módulo. Las dependencias se resuelven desde Google, Maven Central y Gradle Plugin Portal.

- Android Gradle Plugin 9.3.3 y KSP 2.3.4 definidos en gradle/libs.versions.toml.
- compileSdk 37, targetSdk 37 y minSdk 28 en app/build.gradle.kts.
- Código Java compatible con versión 11; Kotlin es el lenguaje del código fuente.
- Versión de aplicación 1.0, versionCode 1 y applicationId com.example.appfitness.
- View Binding y Data Binding habilitados en el módulo.

Para construir una versión de depuración, abrir una terminal en la raíz y ejecutar .\gradlew.bat assembleDebug. El APK resultante se ubica normalmente en app/build/outputs/apk/debug. La compilación de esta revisión terminó correctamente. El manifiesto produjo una advertencia porque CAMERA aparece dos veces.

Para probar la app, usar un emulador o dispositivo con Android API 28 o superior. Si se demostrará la fotografía, el dispositivo necesita una aplicación de cámara capaz de responder a la captura. Para cargar el mapa se necesita conexión a internet. El botón GPS necesita que el dispositivo tenga ubicación activa y una última posición disponible.

## 3 Organización del repositorio

La raíz contiene los archivos Gradle, el wrapper gradlew.bat y el catálogo de versiones. app/build.gradle.kts configura el módulo y sus dependencias. app/src/main/AndroidManifest.xml declara permisos, FileProvider y actividades. app/src/main/java/com/example/appfitness contiene MainActivity y los paquetes ui y data. app/src/main/res contiene layouts, navegación, menú, textos, colores, temas, fuentes, iconos e imágenes.

En ui están LoginActivity, RegistroActivity, GuardarPublicacion, FragmentoFeed y ProfileFragment. LeaderboardFragment se encuentra físicamente en ui/adapter, aunque su paquete Kotlin declarado es com.example.appfitness.ui. En data hay adaptadores de RecyclerView. En data/local están AppDatabase, cuatro entidades, cuatro DAO y LeaderboardAdapter. El directorio test contiene una prueba unitaria de ejemplo y androidTest una prueba instrumentada de ejemplo.

Los nombres de algunos archivos reflejan una evolución del proyecto: GuardarPublicacion funciona como formulario de entrenamiento y FragmentoFeed funciona como historial de entrenamientos. La documentación usa el comportamiento actual como referencia.

## 4 Arquitectura y navegación

La interfaz utiliza actividades para la entrada y una actividad principal con fragmentos para el resto de pantallas. AndroidManifest.xml marca LoginActivity como LAUNCHER. Desde allí se abre RegistroActivity o, si las credenciales coinciden, MainActivity. Al abrir MainActivity, onCreate infla ActivityMainBinding y coloca el layout principal en pantalla.

activity_main.xml contiene una barra superior, un FragmentContainerView con NavHostFragment, un BottomNavigationView y un botón flotante. MainActivity obtiene el NavController del host y llama a setupWithNavController para vincular el menú inferior con el grafo. El botón flotante navega a nav_dumbell, que corresponde al formulario.

nav_graph.xml declara cuatro destinos: nav_home para FragmentoFeed, nav_dumbell para GuardarPublicacion, nav_leaderb para LeaderboardFragment y nav_profile para ProfileFragment. El destino inicial es nav_home. Los ID del menú bottom_navigation_menu.xml coinciden con los del grafo, lo que permite que Navigation haga la selección de pantalla.

View Binding genera clases a partir de los layouts y permite acceder a elementos como binding.btnGuardar. Los fragmentos mantienen una propiedad _binding anulable, la crean en onCreateView y la limpian en onDestroyView. Las lecturas y escrituras de Room usan funciones suspendidas dentro de lifecycleScope con Dispatchers.IO; las actualizaciones de la interfaz vuelven a Dispatchers.Main.

### Flujo de datos de un entrenamiento

1. La persona completa el formulario en GuardarPublicacion.
2. El fragmento comprueba que rutina, ejercicio, peso, repeticiones y duración no estén vacíos.
3. El código convierte los tres valores numéricos y construye EntrenamientoEntity con la fecha actual.
4. EntrenamientoDao.insertarEntrenamiento devuelve el ID generado por Room.
5. Se crea SerieEntity con ese entrenamientoId, numeroSerie igual a 1, peso y repeticiones.
6. SerieDao.insertarSerie guarda la serie y la interfaz muestra una confirmación.
7. FragmentoFeed consulta los entrenamientos y EntrenamientoAdapter los presenta en Inicio.

La inserción de entrenamiento y serie no está envuelta en una transacción explícita. Si la segunda inserción falla, el entrenamiento puede quedar guardado sin su serie. Las entidades tampoco declaran claves foráneas formales de Room.

## 5 Tecnologías y dependencias

Kotlin implementa la lógica. XML define las vistas. Material Components y AndroidX aportan componentes visuales, Activity, AppCompat, ConstraintLayout, Fragment, Lifecycle y Navigation. RecyclerView presenta las listas. Room encapsula SQLite y KSP genera código de persistencia. Las corrutinas permiten ejecutar consultas fuera del hilo de interfaz.

MapLibre SDK dibuja el mapa; el estilo configurado apunta a tiles.openfreemap.org/styles/liberty. Play Services Location proporciona FusedLocationProviderClient para consultar lastLocation. Activity Result API solicita el permiso de cámara y recibe el resultado de TakePicture. FileProvider comparte un URI seguro con la aplicación de cámara. app/build.gradle.kts también declara Google Maps, CameraX, jBCrypt y material-icons-extended; no se observó uso de esas bibliotecas en el flujo principal revisado.

## 6 Base de datos local

AppDatabase es una subclase de RoomDatabase anotada con cuatro entidades. Su versión es 3, el archivo se llama App_fitness y exportSchema está desactivado. obtenerBaseDatos implementa un singleton con @Volatile y synchronized para evitar crear varias instancias. La configuración usa fallbackToDestructiveMigration: si falta una migración para un cambio de esquema, Room puede reconstruir la base y perder datos previos.

### Tabla login

UsuarioEntity se almacena en login. id es entero autogenerado; usuario contiene el nombre visible; email sirve para buscar la cuenta; contrasena guarda el valor introducido. UsuarioDao ofrece buscarCorreo, obtenerUsuarioActual, insertarUsuario y obtenerTodosUsuarios. obtenerUsuarioActual devuelve el primer registro según id; no representa una sesión autenticada.

### Tabla ejercicios

EjercicioEntity se almacena en ejercicios. Tiene id, nombre, grupoMuscular y descripcion. EjercicioDao ofrece consultas de todos los ejercicios o por grupo y métodos de inserción individual o de lista. El formulario actual no utiliza este DAO y no se observó una carga inicial del catálogo.

### Tabla entrenamientos

EntrenamientoEntity se almacena en entrenamientos. Sus campos son id autogenerado, usuarioId, nombreRutina, fecha como texto, duracionMinutos, latitud y longitud opcionales, ubicacionNombre opcional, fotoRuta opcional, fotoTiempo opcional y timestamp en milisegundos. El constructor asigna timestamp con System.currentTimeMillis cuando no se proporciona uno.

EntrenamientoDao permite buscar por usuario, buscar todos ordenados por id descendente, insertar, obtener registros con foto, reiniciar datos de foto, eliminar por fecha y buscar o eliminar por timestamp anterior a un límite. El flujo activo usa insertarEntrenamiento, obtenerTodosEntrenamientos, obtenerEntrenamientosPorUsuario, obtenerRegistroViejo y eliminarEntreno. Los otros métodos existen en el DAO, pero no aparecen en las pantallas revisadas.

### Tabla series

SerieEntity se almacena en series. Sus campos son id, entrenamientoId, nombreEjercicio, numeroSerie, pesoKg y repeticiones. SerieDao permite obtener series de un entrenamiento e insertar una o varias. El formulario inserta una sola serie por entrenamiento y no muestra después el detalle de esas series en Inicio.

### Relaciones y conservación

usuarioId y entrenamientoId funcionan como referencias lógicas, pero no tienen anotaciones @ForeignKey ni índices declarados. El formulario fija usuarioId = 1, independientemente de quién inició sesión. Cuando Inicio elimina entrenamientos de más de 24 horas, no hay eliminación en cascada de series; pueden quedar series sin entrenamiento correspondiente. La foto se intenta eliminar separadamente antes del borrado de cada entrenamiento.

## 7 Pantallas y comportamiento técnico

### Inicio de sesión

LoginActivity carga activity_login.xml. El enlace de registro abre RegistroActivity. El botón Validar Datos obtiene correo y contraseña, elimina espacios externos con trim y exige que ambos estén presentes. En Dispatchers.IO consulta UsuarioDao.buscarCorreo. Si la cuenta existe, compara la contraseña con igualdad directa, muestra un mensaje y abre MainActivity; si falla, informa contraseña incorrecta o correo no registrado.

El inicio de sesión no guarda un token, preferencia ni ID de usuario activo. Tras llegar a MainActivity, las demás pantallas no pueden identificar de manera fiable a la persona que accedió. No se implementó cierre de sesión ni restablecimiento de contraseña.

### Registro de usuario

RegistroActivity usa activity_registro.xml. Recoge nombre, correo y contraseña, exige que no estén vacíos y busca si el correo ya existe. Si no existe, inserta UsuarioEntity y vuelve al login con finish. El botón para volver al inicio de sesión también termina la actividad. No hay comprobación formal del formato del correo ni longitud o fortaleza de la contraseña. La tabla no declara email como único, por lo que dos solicitudes simultáneas podrían insertar el mismo correo.

### Formulario de entrenamiento

GuardarPublicacion usa fragment_guardar_publicacion.xml con un ScrollView. Tiene campos para rutina, ejercicio, peso en kg, repeticiones, duración en minutos y ubicación. También incluye botones para foto, ubicación y guardado, una vista previa de foto y un MapView. Los cinco primeros campos son obligatorios; ubicación y foto son opcionales.

El peso se convierte con toDouble y las repeticiones y duración con toInt. No se comprueban rangos positivos ni se captura NumberFormatException. Por ejemplo, un número incompatible con el formato esperado puede provocar un cierre. La fecha se guarda como yyyy-MM-dd según Locale.getDefault. Después del guardado, limpiarFormulario borra campos y referencias de foto.

El ImageView fotoPrev existe en XML, pero el callback de foto no lo hace visible ni le asigna la imagen; la captura se confirma con un mensaje. El archivo original se crea en filesDir y se expone mediante FileProvider. El callback intenta decodificar y comprimir la imagen JPEG; al guardar, fotoRuta recibe el URI de FileProvider. EntrenamientoAdapter usa setImageURI para mostrar la imagen si el URI sigue siendo accesible.

### Mapa y ubicación

MapLibre se inicializa al crear el fragmento. onMapReady aplica un estilo remoto y registra un listener de toques. Tocar un punto quita el marcador anterior, añade uno nuevo y escribe un texto como Lat: 14.0000, Lon: -90.0000 en el campo de ubicación. Ese texto se guarda en ubicacionNombre; latitud y longitud de EntrenamientoEntity permanecen nulas.

El botón Guardar Ubicación comprueba si ya existe permiso de ubicación fina o aproximada y solicita lastLocation. Si obtiene una posición, centra el mapa y muestra un marcador; no escribe esa posición en el campo de ubicación ni en la entidad. Si no hay permiso, muestra un mensaje, pero el código no inicia una solicitud de permiso de ubicación. lastLocation puede ser nula aunque haya permiso, por ejemplo si el dispositivo aún no dispone de una ubicación conocida.

El fragmento reenvía a MapView los eventos onStart, onResume, onPause, onStop, onSaveInstanceState, onLowMemory y onDestroy. Esto mantiene el ciclo de vida del mapa alineado con el del fragmento.

### Inicio e historial

FragmentoFeed crea EntrenamientoAdapter y un RecyclerView vertical. Carga datos en onViewCreated y vuelve a cargarlos en onResume. Antes de consultar la lista, calcula el instante actual menos 24 horas, busca entrenamientos anteriores y ejecuta eliminarEntreno. Intenta borrar archivos de foto a partir de la ruta del URI; un URI de FileProvider no siempre se corresponde con una ruta de archivo utilizable directamente, por lo que la limpieza física de fotos no está garantizada.

EntrenamientoAdapter muestra nombre de rutina, fecha, duración y foto. No presenta ejercicio, peso, repeticiones ni ubicación aunque esos datos puedan existir en la base. Al actualizar la lista llama a notifyDataSetChanged. No hay filtro por usuario: Inicio muestra todos los entrenamientos que permanecen en la base local.

### Perfil

ProfileFragment consulta el primer usuario de login y sus entrenamientos. Si no hay usuario, muestra valores genéricos. Si encuentra uno, muestra nombre, correo y total. Como no existe una sesión, ese usuario puede no ser quien inició sesión. El conteo también depende de los entrenamientos que queden después de la limpieza de 24 horas.

### Leaderboard

LeaderboardFragment obtiene todos los usuarios y para cada uno consulta sus entrenamientos. Cuenta fechas distintas con map, distinct y size, ordena de mayor a menor y entrega la lista al adaptador. La vista muestra posición, nombre y un texto “días de racha”. Ese cálculo mide días únicos con registros, no una racha de días consecutivos. Además, las consultas se realizan una por usuario y los entrenamientos actuales se asignan siempre a usuarioId 1.

## 8 Recursos visuales y configuración Android

Los layouts XML se encuentran en res/layout. activity_login y activity_registro definen tarjetas con campos y botones. activity_main define la estructura común. Los cuatro fragmentos tienen layouts específicos; item_usuario y item_leaderboard son las tarjetas de las listas. view_pesas.xml existe como vista simple, pero no aparece conectado con la navegación revisada.

res/values/strings.xml contiene el nombre de la aplicación y títulos del menú. La interfaz combina textos en recursos y textos escritos directamente en XML o Kotlin. res/values/colors.xml define blanco y negro y strings.xml incluye verde_alien. Los diseños utilizan además valores hexadecimales directos. themes.xml usa Theme.Material3.DayNight.NoActionBar y las fuentes del proyecto; hay variantes de tema nocturno, fuentes, drawables e iconos de lanzamiento.

AndroidManifest.xml declara permisos CAMERA, ACCESS_FINE_LOCATION y ACCESS_COARSE_LOCATION, y una cámara opcional mediante uses-feature required=false. CAMERA aparece dos veces. FileProvider usa la autoridad derivada del applicationId y res/xml/file_paths.xml. Este último declara rutas de archivos internos, externos y caché; la entrada external-files-path mis_fotos está duplicada. El manifiesto permite backup y remite a backup_rules.xml y data_extraction_rules.xml, que conservan configuraciones de ejemplo sin una política personalizada.

## 9 Manual de usuario

### Primer acceso

1. Instalar y abrir Fit Yourself. La primera pantalla es Inicio de Sesión.
2. Si no se dispone de cuenta, tocar “Regístrate aquí”.
3. Escribir nombre de usuario, correo y contraseña, y pulsar Registrarse.
4. Al volver al inicio, escribir el correo y la contraseña y pulsar Validar Datos.

Si algún campo está vacío, la app pide completarlo. Si el correo ya está registrado, muestra un aviso. Si la contraseña no coincide o el correo no existe, informa el problema sin entrar. Los datos de cuenta viven en la instalación local.

### Registrar una sesión de entrenamiento

1. Abrir Entreno desde el menú inferior o el botón flotante de suma.
2. Escribir el nombre de la rutina, por ejemplo Pecho, y el ejercicio, por ejemplo Press de banca.
3. Escribir peso en kg, cantidad de repeticiones y duración en minutos.
4. Opcionalmente, escribir el nombre del lugar o tocar un punto del mapa para llevar sus coordenadas al campo Ubicación.
5. Opcionalmente, pulsar Tomar Foto y conceder permiso de cámara cuando Android lo solicite.
6. Pulsar Guardar Entrenamiento y esperar el mensaje de confirmación.
7. Abrir Inicio para ver la tarjeta con la rutina, fecha, duración y foto si se capturó.

El botón Guardar Ubicación obtiene la última posición GPS y centra el mapa, pero no la agrega al campo que se guarda. Si se necesita conservar una ubicación, escribirla o tocar directamente el mapa. No hay vista detallada de la serie después del guardado.

### Consultar las otras pantallas

Inicio presenta entrenamientos recientes. Los registros con más de 24 horas se borran al cargar o reanudar esta pantalla. Perfil muestra datos del primer usuario creado en el dispositivo y un total de entrenamientos asociado a ese ID. Leaderboard ordena usuarios según la cantidad de fechas diferentes con entrenamiento. Los rótulos de perfil y clasificación no deben interpretarse como una sesión personal fiable ni como una racha consecutiva.

### Problemas frecuentes

- El mapa no aparece: comprobar la conexión a internet; el estilo se carga desde OpenFreeMap.
- El GPS no muestra una ubicación: comprobar servicios de ubicación y permiso en ajustes de Android; lastLocation puede no tener aún un valor.
- La cámara no abre: comprobar que exista una aplicación de cámara y que el permiso CAMERA esté concedido.
- El formulario se cierra al guardar: revisar que peso, repeticiones y duración tengan valores numéricos válidos; la versión actual no captura errores de conversión.
- Un entrenamiento desapareció: Inicio elimina registros anteriores a 24 horas al abrirse o reanudarse.
- El perfil muestra otra cuenta: la aplicación todavía no mantiene el ID de la sesión iniciada.

## 10 Seguridad privacidad y calidad de datos

La contraseña se guarda y compara en texto claro. jBCrypt figura como dependencia, pero no se usa en el registro o login revisados. Esta versión debe tratarse como prototipo local; antes de distribuirla se necesita hash de contraseñas y una estrategia para migrar cuentas existentes.

La aplicación solicita acceso a cámara y consulta permisos de ubicación. Las fotos se guardan en archivos de la aplicación y sus URI en Room. No hay una política de copia de seguridad ajustada a datos sensibles: el manifiesto permite backup y los XML de reglas son plantillas. Conviene revisar qué datos de cuenta, entrenamientos y fotos deberían incluirse o excluirse.

La validez de datos es básica. No se comprueba formato de correo, unicidad a nivel SQL, números positivos, límites razonables de duración o peso ni consistencia transaccional entre entrenamiento y serie. Tampoco hay relaciones con claves foráneas, por lo que pueden quedar referencias huérfanas después de eliminar entrenamientos.

## 11 Pruebas y estado de verificación

app/src/test contiene ExampleUnitTest, que verifica 2 + 2 = 4. app/src/androidTest contiene ExampleInstrumentedTest, que comprueba el nombre de paquete. Estas pruebas son ejemplos de plantilla y no cubren registro, login, persistencia, cámara, ubicación, retención, perfil ni Leaderboard.

Durante la revisión se ejecutó .\gradlew.bat assembleDebug y la compilación fue satisfactoria. Gradle informó una advertencia por la declaración duplicada de CAMERA en AndroidManifest.xml. La compilación demuestra que el proyecto produce un APK de depuración; no demuestra que todos los flujos funcionen en cada dispositivo. Esta documentación describe el código y no sustituye pruebas manuales de la interfaz ni pruebas automatizadas de comportamiento.

Para verificar funcionalmente una siguiente versión, probar al menos: registro correcto y duplicado; login válido, inválido y campos vacíos; valores numéricos erróneos; guardado con y sin foto; selección manual de mapa; GPS con permiso, sin permiso y sin lastLocation; aparición y vencimiento de entrenamientos; asociación de series; perfil con varias cuentas; y clasificación con fechas consecutivas y no consecutivas.

## 12 Mantenimiento y mejoras recomendadas

Primero, implementar una sesión local que conserve el ID del usuario autenticado y usarlo al insertar entrenamientos, filtrar Inicio y construir Perfil. Segundo, proteger contraseñas con hash y migrar las existentes. Tercero, validar correo y campos numéricos, y guardar entrenamiento y serie en una transacción. Estas medidas corrigen los riesgos más visibles del flujo principal.

Después, decidir una política explícita de conservación: la eliminación automática de 24 horas impide un historial duradero. Si se mantiene, eliminar también las series relacionadas mediante claves foráneas y cascada; si se busca seguimiento a largo plazo, retirar ese borrado. Añadir migraciones Room explícitas para preservar datos en futuras versiones.

Por último, completar el comportamiento de ubicación: solicitar permiso en tiempo de ejecución, guardar latitud y longitud reales, reflejar el GPS en el campo si corresponde y manejar ausencia de señal. Mostrar vista previa de foto y asegurar la eliminación de archivos por una ruta interna controlada. Unificar textos en strings.xml, reducir dependencias sin uso y añadir pruebas sobre los casos críticos.

## 13 Glosario y archivos de consulta

Activity: pantalla Android que aloja una interfaz. Fragment: sección reutilizable alojada por MainActivity. NavController: componente que cambia destinos del grafo. View Binding: clases generadas que permiten acceder a vistas XML. Room: capa de acceso a SQLite. Entity: clase que representa una tabla. DAO: interfaz con consultas y operaciones de datos. Corrutina: mecanismo de Kotlin para trabajo asíncrono. FileProvider: proveedor que comparte archivos mediante URI. RecyclerView: componente que presenta listas eficientes.

Para estudiar el flujo, comenzar por AndroidManifest.xml, LoginActivity.kt, RegistroActivity.kt, MainActivity.kt, nav_graph.xml y GuardarPublicacion.kt. Continuar con AppDatabase.kt, las Entity y DAO, FragmentoFeed.kt, ProfileFragment.kt y LeaderboardFragment.kt. Para la apariencia, revisar los XML de res/layout, el menú inferior y los recursos de values.
