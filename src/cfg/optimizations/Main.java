package cfg.optimizations;

import cfg.PrettyPrintVisitor;

public class Main
{
  public cfg.program.T program;
  
  // gen, kill for statements
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmGen;
  public java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmKill;
  
  private java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveIn;
  private java.util.HashMap<cfg.stm.T, java.util.HashSet<String>> stmLiveOut;

  private java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>> stmReachIn;
  private java.util.HashMap<cfg.stm.T, java.util.HashSet<cfg.stm.T>> stmReachOut;

  
  public void accept(cfg.program.T cfg)
  {
    // liveness analysis
    LivenessVisitor liveness = new LivenessVisitor();
    control.CompilerPass livenessPass = new control.CompilerPass(
        "Liveness analysis", cfg, liveness);
    if (control.Control.skipPass("cfg.Linvess")) {
    } else {
      livenessPass.doit();
      // Export necessary data structures from the
      // liveness analysis.
      // Your code here:
      this.stmGen = liveness.stmGen;
      this.stmKill = liveness.stmKill;
      this.stmLiveIn = liveness.stmLiveIn;
      this.stmLiveOut = liveness.stmLiveOut;
    }

    // dead-code elimination
    DeadCode deadCode = new DeadCode();
    deadCode.stmLiveIn = this.stmLiveIn;
    deadCode.stmLiveOut = this.stmLiveOut;
    control.CompilerPass deadCodePass = new control.CompilerPass(
        "Dead-code elimination", cfg, deadCode);
    if (control.Control.skipPass("cfg.deadCode")) {
    } else {
      deadCodePass.doit();
      cfg = deadCode.program;

      if (control.Control.isTracing("cfg.deadCode")) {
	      cfg.PrettyPrintVisitor ppCfg = new cfg.PrettyPrintVisitor();
	      control.CompilerPass ppCfgCodePass = new control.CompilerPass(
	          "C code printing", cfg, ppCfg);
	      ppCfgCodePass.doit();
      }
    }

    // reaching definition
    ReachingDefinition reachingDef = new ReachingDefinition();
    reachingDef.stmGen = this.stmGen;
    reachingDef.stmKill = this.stmKill;
    control.CompilerPass reachingDefPass = new control.CompilerPass(
        "Reaching definition", cfg, reachingDef);
    if (control.Control.skipPass("cfg.reaching")) {
    } else {
      reachingDefPass.doit();
      // Export necessary data structures
      // Your code here:
      this.stmReachIn = reachingDef.stmIn;
      this.stmReachOut = reachingDef.stmOut;
    }

    // constant propagation
    ConstProp constProp = new ConstProp();
    constProp.stmIn = this.stmReachIn;
    constProp.stmOut = this.stmReachOut;
    control.CompilerPass constPropPass = new control.CompilerPass(
        "Constant propagation", cfg, constProp);
    if (control.Control.skipPass("cfg.constProp")) {
    } else {
      constPropPass.doit();
      cfg = constProp.program;
    }

    // copy propagation
    CopyProp copyProp = new CopyProp();
    copyProp.stmIn = this.stmReachIn;
    copyProp.stmOut = this.stmReachOut;
    control.CompilerPass copyPropPass = new control.CompilerPass(
        "Copy propagation", cfg, copyProp);
    if (control.Control.skipPass("cfg.copyProp")) {
    } else {
      copyPropPass.doit();
      cfg = copyProp.program;
    }

    // available expression
    AvailExp availExp = new AvailExp();
    control.CompilerPass availExpPass = new control.CompilerPass(
        "Available expression", cfg, availExp);
    if (control.Control.skipPass("cfg.availExp")) {
    } else {
      availExpPass.doit();
      // Export necessary data structures
      // Your code here:
      
      /* YKG. I think I do not need to do this calculation. */
    }

    // CSE
    Cse cse = new Cse();
    control.CompilerPass csePass = new control.CompilerPass(
        "Common subexpression elimination", cfg, cse);
    if (control.Control.skipPass("cfg.cse")) {
    } else {
      csePass.doit();
      cfg = cse.program;
    }

    program = cfg;

    return;
  }
}
