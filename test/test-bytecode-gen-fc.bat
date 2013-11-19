@echo ==================================================
@echo test starting
@echo =============
for %%i in (*.java) do javac %%i
for %%i in (*.java) do java %%~ni > %%~ni.j.txt
for %%i in (*.java) do java -cp ../bin Tiger %%i -codegen bytecode 1>%%~ni.bytecode.txt
for %%i in (*.java) do fc %%~ni.j.txt %%~ni.bytecode.txt
@echo =============
@echo test finished
@echo ==================================================
