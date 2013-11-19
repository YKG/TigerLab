@echo ==================================================
@echo test starting
@echo =============
for %%i in (*.java) do java -cp ../bin Tiger %%i -codegen bytecode 1>%%i.bytecode.txt
@echo =============
@echo test finished
@echo ==================================================
