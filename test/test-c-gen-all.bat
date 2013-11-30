@echo ==================================================
@echo test starting
@echo =============
for %%i in (*.java) do java -cp ../bin Tiger %%i -codegen C >%%~ni.c.txt
@echo =============
@echo test finished
@echo ==================================================
