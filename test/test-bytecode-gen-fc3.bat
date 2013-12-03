@echo ==================================================
@echo test starting
@echo =============
for %%i in (*.java) do java %%~ni 1>%%~ni.bytecode.txt
@echo =============
@echo test finished
@echo ==================================================
