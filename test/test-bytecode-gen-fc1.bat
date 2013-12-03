@echo ==================================================
@echo test starting
@echo =============
for %%i in (*.java) do java -cp ../bin Tiger %%i -codegen bytecode
@echo =============
@echo test finished
@echo ==================================================
