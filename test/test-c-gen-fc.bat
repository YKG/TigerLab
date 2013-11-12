@echo ==================================================
@echo test starting
@echo =============
for %%i in (*.java) do javac %%i
for %%i in (*.java) do java %%~ni > %%~ni.j.txt
for %%i in (*.java) do java -cp ../bin Tiger %%i -codegen C >%%~ni.c.txt
for %%i in (*.java) do fc %%~ni.j.txt %%~ni.c.txt
@echo =============
@echo test finished
@echo ==================================================
