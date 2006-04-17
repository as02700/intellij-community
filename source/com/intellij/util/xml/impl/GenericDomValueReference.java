/*
 * Copyright (c) 2000-2006 JetBrains s.r.o. All Rights Reserved.
 */
package com.intellij.util.xml.impl;

import com.intellij.javaee.J2EEBundle;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.resolve.reference.PsiReferenceProvider;
import com.intellij.psi.impl.source.resolve.reference.ReferenceType;
import com.intellij.psi.impl.source.resolve.reference.impl.GenericReference;
import com.intellij.psi.xml.XmlElement;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.xml.*;

import java.util.List;

/**
 * author: lesya
 */
public class GenericDomValueReference<T> extends GenericReference {
  private final GenericDomValue<T> myGenericValue;
  private final XmlElement myContextElement;
  private final TextRange myTextRange;
  private final XmlElement myValueElement;

  public GenericDomValueReference(final PsiReferenceProvider provider, GenericDomValue<T> xmlValue) {
    super(provider);
    myGenericValue = xmlValue;
    final XmlTag tag = xmlValue.getXmlTag();
    myValueElement = xmlValue instanceof GenericAttributeValue
                     ? ((GenericAttributeValue)xmlValue).getXmlAttributeValue()
                     : tag.getValue().getTextElements()[0];
    myContextElement = (XmlElement)myValueElement.getParent();
    final TextRange range = getTextRange(myValueElement.getText());
    myTextRange = range.shiftRight(myValueElement.getTextRange().getStartOffset() - myContextElement.getTextRange().getStartOffset());
  }

  protected final PsiManager getPsiManager() {
    return PsiManager.getInstance(myGenericValue.getManager().getProject());
  }

  protected final XmlElement getValueElement() {
    return myValueElement;
  }

  protected TextRange getTextRange(String text) {
    final String trimmedText = text.trim();
    final int inside = text.indexOf(trimmedText);
    return new TextRange(inside, inside + trimmedText.length());
  }

  protected final GenericDomValue<T> getGenericValue() {
    return myGenericValue;
  }

  public XmlElement getContext() {
    return myContextElement;
  }

  public PsiReference getContextReference() {
    return null;
  }

  public ReferenceType getType() {
    return new ReferenceType(ReferenceType.UNKNOWN);
  }

  protected PsiElement resolveInner(T o) {
    if (o instanceof PsiElement) {
      return (PsiElement)o;
    }
    if (o instanceof DomElement) {
      return ((DomElement)o).getXmlTag();
    }
    if (o instanceof ModelMerger.MergedObject) {
      final List<T> list = ((ModelMerger.MergedObject<T>)o).getImplementations();
      for (final T o1 : list) {
        final PsiElement psiElement = resolveInner(o1);
        if (psiElement != null) {
          return psiElement;
        }
      }
    }
    return null;
  }

  public PsiElement resolveInner() {
    final T value = myGenericValue.getValue();
    return value == null ? null : resolveInner(value);
  }

  public ReferenceType getSoftenType() {
    return getType();
  }

  public boolean needToCheckAccessibility() {
    return false;
  }

  public XmlElement getElement() {
    return myContextElement;
  }

  public TextRange getRangeInElement() {
    return myTextRange;
  }

  public String getCanonicalText() {
    String value = myGenericValue.getStringValue();
    if (value != null) {
      final TextRange textRange = getTextRange(value);
      return value.substring(textRange.getStartOffset(), textRange.getEndOffset());
    }
    return J2EEBundle.message("unknown.j2ee.reference.canonical.text");
  }

  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    myGenericValue.setStringValue(newElementName);
    return myGenericValue.getXmlTag();
  }

  public PsiElement bindToElement(PsiElement element) throws IncorrectOperationException {
    if (element instanceof XmlTag) {
      DomElement domElement = myGenericValue.getManager().getDomElement((XmlTag) element);
      if (domElement != null) {
        myGenericValue.setStringValue(domElement.getGenericInfo().getElementName(domElement));
      } else {
        myGenericValue.setStringValue(((XmlTag)element).getName());
      }
      return myGenericValue.getXmlTag();
    }
    return null;
  }

}
