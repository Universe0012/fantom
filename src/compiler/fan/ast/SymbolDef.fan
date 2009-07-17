//
// Copyright (c) 2009, Brian Frank and Andy Frank
// Licensed under the Academic Free License version 3.0
//
// History:
//   13 Jul 09  Brian Frank  Creation
//

**
** SymbolDef
**
class SymbolDef : DefNode, CSymbol
{

//////////////////////////////////////////////////////////////////////////
// Construction
//////////////////////////////////////////////////////////////////////////

  new make(Location location, CompilationUnit unit, CType? of, Str name, Expr val)
    : super(location)
  {
    this.unit  = unit
    this.ctype = of
    this.name  = name
    this.val   = val
  }

//////////////////////////////////////////////////////////////////////////
// CSymbol
//////////////////////////////////////////////////////////////////////////

  override PodDef pod() { unit.pod }

  readonly CompilationUnit unit

  override readonly Str name

  override Str qname() { "${pod.name}::${name}" }

  override CType of()
  {
    if (ctype == null) throw Err("symbol not typed yet $qname")
    return ctype
  }

//////////////////////////////////////////////////////////////////////////
// Methods
//////////////////////////////////////////////////////////////////////////

  Void walk(Visitor v, VisitDepth depth)
  {
    v.enterSymbolDef(this)
    if (depth >= VisitDepth.expr) val = val.walk(v)
    v.exitSymbolDef(this)
  }

  override Void print(AstWriter out)
  {
    out.w(toStr).nl
  }

//////////////////////////////////////////////////////////////////////////
// Documentation
//////////////////////////////////////////////////////////////////////////

  override [Str:Str]? docMeta()
  {
    return ["def": val.toDocStr]
  }

//////////////////////////////////////////////////////////////////////////
// Fields
//////////////////////////////////////////////////////////////////////////

  CType? ctype
  Expr val

}