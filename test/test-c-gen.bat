@echo ==================================================
@echo test starting
@echo =============
for %%i in (*.java) do java -cp ../bin Tiger %%i -codegen C
@echo =============
@echo test finished
@echo ==================================================
