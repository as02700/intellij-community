package org.jetbrains.plugins.groovy.lang.psi.impl.statements.expressions.arithmetic;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiType;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.util.TypeConversionUtil;
import com.intellij.util.containers.HashMap;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TObjectIntHashMap;
import org.jetbrains.plugins.groovy.lang.psi.api.statements.expressions.GrBinaryExpression;
import org.jetbrains.annotations.NonNls;

import java.util.Map;

/**
 * @author ven
 */
public class TypesUtil {
  @NonNls
  public static final Map<String, PsiType> ourQNameToUnboxed = new HashMap<String, PsiType>();

  public static PsiType getNumericResultType(GrBinaryExpression binaryExpression) {
    PsiType lType = binaryExpression.getLeftOperand().getType();
    PsiType rType = binaryExpression.getRightOperand().getType();
    if (lType == null || rType == null) return null;
    String lCanonical = lType.getCanonicalText();
    String rCanonical = rType.getCanonicalText();
    if (TYPE_TO_RANK.containsKey(lCanonical) && TYPE_TO_RANK.containsKey(rCanonical)) {
      int lRank = TYPE_TO_RANK.get(lCanonical);
      int rRank = TYPE_TO_RANK.get(rCanonical);
      int resultRank = Math.max(lRank, rRank);
      return binaryExpression.getManager().getElementFactory().createTypeByFQClassName(RANK_TO_TYPE.get(resultRank), binaryExpression.getResolveScope());
    }
    return null;
  }

  private static final TObjectIntHashMap<String> TYPE_TO_RANK = new TObjectIntHashMap<String>();
  static {
    TYPE_TO_RANK.put("java.lang.Byte", 1);
    TYPE_TO_RANK.put("java.lang.Short", 2);
    TYPE_TO_RANK.put("java.lang.Character", 2);
    TYPE_TO_RANK.put("java.lang.Integer", 3);
    TYPE_TO_RANK.put("java.lang.Long", 4);
    TYPE_TO_RANK.put("java.math.BigInteger", 5);
    TYPE_TO_RANK.put("java.math.BigDecimal", 6);
    TYPE_TO_RANK.put("java.lang.Float", 7);
    TYPE_TO_RANK.put("java.lang.Double", 8);
  }

  static {
    ourQNameToUnboxed.put("java.lang.Boolean", PsiType.BOOLEAN);
    ourQNameToUnboxed.put("java.lang.Byte", PsiType.BYTE);
    ourQNameToUnboxed.put("java.lang.Character", PsiType.CHAR);
    ourQNameToUnboxed.put("java.lang.Short", PsiType.SHORT);
    ourQNameToUnboxed.put("java.lang.Integer", PsiType.INT);
    ourQNameToUnboxed.put("java.lang.Long", PsiType.LONG);
    ourQNameToUnboxed.put("java.lang.Float", PsiType.FLOAT);
    ourQNameToUnboxed.put("java.lang.Double", PsiType.DOUBLE);
  }


  private static final TIntObjectHashMap<String> RANK_TO_TYPE = new TIntObjectHashMap<String>();
  static {
    RANK_TO_TYPE.put(1, "java.lang.Integer");
    RANK_TO_TYPE.put(2, "java.lang.Integer");
    RANK_TO_TYPE.put(3, "java.lang.Integer");
    RANK_TO_TYPE.put(4, "java.lang.Long");
    RANK_TO_TYPE.put(5, "java.lang.BigInteger");
    RANK_TO_TYPE.put(6, "java.math.BigDecimal");
    RANK_TO_TYPE.put(7, "java.math.Double");
    RANK_TO_TYPE.put(8, "java.lang.Double");
  }

  public static boolean isAssignable(PsiType lType, PsiType rType, PsiManager manager, GlobalSearchScope scope) {
    //all numeric types are assignable
    if (isNumericType(lType)) {
      return isNumericType(rType) || rType.equalsToText("java.lang.String");
    } else {
      if (lType.equalsToText("java.lang.String") && isNumericType(rType)) return true;
      rType = boxPrimitiveTypeAndEraseGenerics(rType, manager, scope);
    }

    lType = TypeConversionUtil.erasure(lType);
    rType = TypeConversionUtil.erasure(rType);
    return TypeConversionUtil.isAssignable(lType, rType);
  }

  private static boolean isNumericType(PsiType type) {
    if (type instanceof PsiClassType) {
      return TYPE_TO_RANK.contains(type.getCanonicalText());
    }

    return type instanceof PsiPrimitiveType &&
           TypeConversionUtil.isNumericType(type);
  }

  public static PsiType unboxPrimitiveTypeAndEraseGenerics(PsiType result) {
    return TypeConversionUtil.erasure(unboxPrimitiveType(result));
  }

  public static PsiType unboxPrimitiveType(PsiType result) {
    if (result instanceof PsiClassType) {
      PsiType unboxed = ourQNameToUnboxed.get(result.getCanonicalText());
      if (unboxed != null) result = unboxed;
    }
    return result;
  }

  public static PsiType boxPrimitiveTypeAndEraseGenerics(PsiType result, PsiManager manager, GlobalSearchScope resolveScope) {
    if (result instanceof PsiPrimitiveType) {
      PsiPrimitiveType primitive = (PsiPrimitiveType) result;
      String boxedTypeName = primitive.getBoxedTypeName();
      if (boxedTypeName != null) {
        return manager.getElementFactory().createTypeByFQClassName(boxedTypeName, resolveScope);
      }
    }

    return TypeConversionUtil.erasure(result);
  }

  public static PsiType boxPrimitiveType(PsiType result, PsiManager manager, GlobalSearchScope resolveScope) {
    if (result instanceof PsiPrimitiveType) {
      PsiPrimitiveType primitive = (PsiPrimitiveType) result;
      String boxedTypeName = primitive.getBoxedTypeName();
      if (boxedTypeName != null) {
        return manager.getElementFactory().createTypeByFQClassName(boxedTypeName, resolveScope);
      }
    }

    return result;
  }
}
