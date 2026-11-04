# Preparar Ubuntu 20.04 para compilar el APK

Guía para dejar una Ubuntu (20.04 o similar) lista para compilar este proyecto desde la terminal,
sin Android Studio. Versiones usadas: JDK 17, Gradle 8.14.2, plataforma Android 36, build-tools 35.0.0.

## 1. Herramientas básicas y JDK 17

```
sudo apt update
sudo apt install openjdk-17-jdk unzip wget
java -version        # debe decir 17.x
```

## 2. Gradle 8.14.2

```
cd /tmp
wget https://services.gradle.org/distributions/gradle-8.14.2-bin.zip
sudo unzip -d /opt gradle-8.14.2-bin.zip
sudo ln -sfn /opt/gradle-8.14.2 /opt/gradle
echo 'export PATH=/opt/gradle/bin:$PATH' | sudo tee /etc/profile.d/gradle.sh
source /etc/profile.d/gradle.sh
gradle -version      # debe decir 8.14.2
```

## 3. Herramientas de línea de comandos del SDK de Android

Bajá el zip "Command line tools only" (Linux) desde <https://developer.android.com/studio#command-line-tools-only>.
Después, reemplazando el nombre del archivo por el que bajaste:

```
sudo mkdir -p /opt/android-sdk/cmdline-tools
sudo unzip -d /opt/android-sdk/cmdline-tools commandlinetools-linux-XXXXXXX_latest.zip
sudo mv /opt/android-sdk/cmdline-tools/cmdline-tools /opt/android-sdk/cmdline-tools/latest
```

Es importante que quede en `/opt/android-sdk/cmdline-tools/latest/bin/sdkmanager`.

## 4. Variables de entorno

```
sudo tee /etc/profile.d/android-sdk.sh <<'EOF'
export ANDROID_HOME=/opt/android-sdk
export ANDROID_SDK_ROOT=/opt/android-sdk
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools
EOF
source /etc/profile.d/android-sdk.sh
```

## 5. Permisos de escritura sobre el SDK

Gradle necesita poder escribir en la carpeta del SDK (si no, falla con "The SDK directory is not writable"):

```
sudo chown -R $USER:$USER /opt/android-sdk
```

## 6. Licencias y componentes

```
yes | sdkmanager --licenses
sdkmanager "platform-tools" "platforms;android-36" "build-tools;35.0.0"
```

`platform-tools` incluye `adb`, para instalar el APK en el teléfono.

## 7. Compilar

```
cd QuienLlama
echo "sdk.dir=/opt/android-sdk" > local.properties
gradle assembleDebug
```

La primera vez baja dependencias y tarda unos minutos. El APK queda en
`app/build/outputs/apk/debug/app-debug.apk`.

## 8. Instalar en el teléfono

Con la depuración USB activada y el teléfono conectado:

```
adb devices
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Problemas comunes

- **"SDK directory is not writable"**: falta el paso 5.
- **"Failed to install the following SDK components"**: falta aceptar licencias (paso 6) o instalar la versión pedida.
- **"sdkmanager: command not found"**: falta cargar las variables (paso 4) o la carpeta `latest` está mal ubicada (paso 3).
- **Cambió la versión que pide el proyecto**: instalá la que indique el error con `sdkmanager "build-tools;X.Y.Z"`.
