@echo ==================================================
@echo test starting
@echo =============
for %%i in (*.java) do java -cp ../bin Tiger %%i -visualize jpg -verbose 1 -dump C
@echo =============
@echo test finished
@echo ==================================================
