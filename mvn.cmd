@echo off
if "%JAVA_HOME%"=="" (
    if exist "C:\Users\kdsth\.gemini\antigravity\brain\855564d7-f8f3-4f92-85dd-12b4a120628d\scratch\jdk-21\bin\javac.exe" (
        set "JAVA_HOME=C:\Users\kdsth\.gemini\antigravity\brain\855564d7-f8f3-4f92-85dd-12b4a120628d\scratch\jdk-21"
    ) else if exist "C:\Users\kdsth\.jdks\ms-21.0.12.1\bin\javac.exe" (
        set "JAVA_HOME=C:\Users\kdsth\.jdks\ms-21.0.12.1"
    ) else (
        set "JAVA_HOME=C:\Program Files\Java\jdk-26"
    )
)
set "PATH=%JAVA_HOME%\bin;%PATH%"
"C:\Users\kdsth\.m2\wrapper\dists\apache-maven-3.9.9-bin\4nf9hui3q3djbarqar9g711ggc\apache-maven-3.9.9\bin\mvn.cmd" %*
