# Quién llama

**[⬇ Descargar el APK](https://github.com/alecelular/QuienLlama/releases/latest)** (Android 10 o superior). Después de instalarlo, seguí la sección "Puesta en marcha".

[English version](README.en.md)

App de Android que muestra el **nombre del contacto** en las llamadas entrantes aunque la operadora
mande el número en un formato distinto al de la agenda.

**El problema:** en Argentina, Claro envía `+549 + área + número`, mientras que Movistar/Personal envían
`0 + área + número`. Si tus contactos están guardados con `+549`, algunos teléfonos (por ejemplo, Samsung
con firmware de otra región) no los reconocen y muestran solo el número.

**La solución:** en lugar de duplicar los contactos sin prefijo, la app compara los **últimos N dígitos** del
número entrante contra la agenda.

## Cómo funciona

1. Prueba con todos los dígitos del número entrante.
2. Si no hay coincidencia, quita un dígito por la izquierda y repite, hasta el mínimo configurado (por defecto 10).
3. En el primer nivel con coincidencias:
   - un solo contacto → muestra su nombre;
   - varios contactos → cartel ámbar. Se elige en la app qué mostrar: **todos los nombres** (útil para detectar
     contactos duplicados), **solo el primero** (orden alfabético, indicando cuántos coinciden) o solo un aviso.
4. Nunca baja a un nivel más corto si ya encontró coincidencias.

Detalles: un contacto coincide si sus dígitos *terminan* igual que el sufijo probado; se cuenta por contacto
(dos números de la misma persona no son ambigüedad); los números más cortos que el mínimo (147, 911…)
solo coinciden por igualdad exacta. La lógica está en
[`SuffixMatcher.kt`](app/src/main/java/com/quienllama/SuffixMatcher.kt), con tests en
[`SuffixMatcherTest.kt`](app/src/test/java/com/quienllama/SuffixMatcherTest.kt).

## Últimas llamadas

El marcador del teléfono dibuja su propia lista de recientes y otra app no puede modificarla. Por eso la app
trae su propia pantalla **"Últimas llamadas"**, con los nombres identificados por el mismo criterio que el cartel.

Además hay una opción **experimental** (apagada por defecto): escribir el nombre en el campo "nombre guardado" del
registro de llamadas, solo en las llamadas que quedaron sin nombre y que la app identifica con un único contacto.
Si el marcador de tu teléfono muestra ese campo, el nombre aparece en su propia lista. Necesita el permiso de
escribir el registro de llamadas; nunca modifica llamadas que ya tienen nombre.

## Privacidad

La app **no tiene permiso de Internet** y no guarda ni envía tus contactos: la agenda se lee en memoria
solo para comparar números. Los permisos del registro de llamadas son opcionales y se usan solo en el teléfono.
Podés comprobarlo en el
[`AndroidManifest.xml`](app/src/main/AndroidManifest.xml).

## Requisitos

- Android 10 o superior.
- Permisos: leer contactos, mostrar sobre otras apps, ser la "app de identificación de llamadas" y, para ver
  el nombre con el teléfono bloqueado, notificaciones y "notificaciones a pantalla completa". Opcionales: leer el registro de llamadas (pantalla
  "Últimas llamadas") y escribirlo (experimento de nombres).

## Compilar

Necesitás JDK 17, Gradle 8.14 o superior y el SDK de Android con la plataforma 36 y las build-tools 35.0.0.

```
echo "sdk.dir=/ruta/a/tu/android-sdk" > local.properties
./gradlew assembleDebug
```

El APK queda en `app/build/outputs/apk/debug/app-debug.apk`. También se puede abrir la carpeta con Android Studio.
Para preparar una Ubuntu desde cero, ver [COMPILAR.md](COMPILAR.md).

## Puesta en marcha

Abrí la app y tocá los cuatro botones hasta que los cuatro queden en ✔. En Samsung, el tercero también está en
Ajustes → Aplicaciones → Elegir apps predeterminadas → App de ID y spam. Después podés usar
"Probar sin llamar" para ver el resultado con cualquier número.

## Limitaciones conocidas

- Probada en un Samsung Galaxy A17 con operadoras Claro y Movistar. Otros fabricantes pueden comportarse distinto.
- El cartel flota sobre la pantalla de llamada (no la reemplaza); su altura se ajusta desde la app.
- El experimento de nombres en el registro depende de que el marcador muestre el campo "nombre guardado"; no está
  garantizado.
- Con el teléfono bloqueado Android no permite ventanas flotantes, así que el cartel se abre como una
  notificación de pantalla completa (necesita el cuarto permiso). Se pide ~1,5 s después de que suena la llamada
  para quedar por encima de la pantalla de llamada. Para diagnosticar: `adb logcat -s QuienLlama`.
- Es posible que el marcador del sistema no avise a la app de números que ya reconoce como contactos.

## Licencia

[PolyForm Noncommercial 1.0.0](https://polyformproject.org/licenses/noncommercial/1.0.0): podés usar,
copiar y modificar la app gratis para fines no comerciales. Para venderla o usarla comercialmente
hace falta permiso del autor.

© 2026 Alejandro F. Fernández (@alecelular en X, Instagram y otras redes)

**Contribuciones:** si enviás cambios (pull requests), se entiende que los cedés bajo esta misma licencia
y que el autor puede reutilizarlos, incluso en versiones comerciales.

## Apoyar el proyecto

Si te resultó útil, podés invitarme un café:
[![Invitame un café](https://cdn.cafecito.app/img/buttons/button_1.svg)](https://cafecito.app/rsp148)
(también hay un botón en la pantalla de la app; solo abre esta página en tu navegador).

---

*Espero que disfruten este proyecto tanto como yo disfruté su
desarrollo.*
