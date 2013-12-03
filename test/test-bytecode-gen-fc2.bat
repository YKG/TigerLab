@echo ==================================================
@echo test starting
@echo =============
for %%i in (*.java) do java -jar ../jasmin.jar %%~ni.j 1>%%~ni.bytecode.txt
@echo =============
@echo test finished
@echo ==================================================
