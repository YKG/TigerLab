@echo ==================================================
@echo test starting
@echo =============
for %%i in (*.txt) do del %%i
for %%i in (*.exe) do del %%i
for %%i in (*.class) do del %%i
for %%i in (*.c) do del %%i
for %%i in (*.j) do del %%i
@echo =============
@echo test finished
@echo ==================================================
