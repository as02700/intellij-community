// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.intellij.openapi.fileTypes;

import com.intellij.openapi.options.Scheme;
import com.intellij.openapi.util.NlsContexts.Label;
import com.intellij.openapi.util.NlsSafe;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.nio.charset.Charset;

/**
 * <p>Describes a filetype.</p>
 *
 * <p>Must be registered via {@code com.intellij.fileType} extension point.
 * If file type depends on given file, {@link com.intellij.openapi.fileTypes.ex.FileTypeIdentifiableByVirtualFile}
 * can be used for non-static mapping.</p>
 *
 * <p>Use {@link LanguageFileType} for files having {@link com.intellij.lang.Language} support.</p>
 *
 * @see com.intellij.openapi.fileTypes.FileTypes
 * @see INativeFileType
 */
public interface FileType extends Scheme {
  FileType[] EMPTY_ARRAY = new FileType[0];

  /**
   * Returns the name of the file type. The name must be unique among all file types registered in the system.
   */
  @Override
  @NonNls @NotNull String getName();

  /**
   * Returns the user-readable description of the file type.
   */
  @Label @NotNull String getDescription();

  /**
   * Returns the default extension for files of the type, <em>not</em> including the leading '.'.
   */
  @NlsSafe @NotNull String getDefaultExtension();

  /**
   * Returns the icon used for showing files of the type, or {@code null} if no icon should be shown.
   */
  @Nullable Icon getIcon();

  /**
   * Returns {@code true} if files of the specified type contain binary data, {@code false} if the file is plain text.
   * Used for source control, to-do items scanning and other purposes.
   */
  boolean isBinary();

  /**
   * Returns {@code true} if the specified file type is read-only. Read-only file types are not shown in the "File Types" settings dialog,
   * and users cannot change the extensions associated with the file type.
   */
  default boolean isReadOnly() {
    return false;
  }

  /**
   * Returns the character set for the specified file.
   *
   * @param file    The file for which the character set is requested.
   * @param content File content.
   * @return The character set name, in the format supported by {@link java.nio.charset.Charset} class.
   */
  default @NonNls @Nullable String getCharset(@NotNull VirtualFile file, byte @NotNull [] content) {
    // TODO see MetadataJsonFileType (it's actually text but tries indexing itself as binary)
    // if (isBinary()) {
    //   throw new UnsupportedOperationException();
    // }
    CharsetHint hint = getCharsetHint();
    if (hint instanceof CharsetHint.ForcedCharset) {
      return ((CharsetHint.ForcedCharset)hint).getCharset().name();
    }
    return null;
  }

  /**
   * A method to specify how {@link FileType}'s charset is evaluated for corresponding files. There are possible cases:
   * <ul>
   *   <li>In cases when charset is always the same for every file of a given file type, one could use {@link CharsetHint.ForcedCharset}.</li>
   *   <li>When file type can be determined using only the file's binary content then {@link CharsetHint#CONTENT_DEPENDENT_CHARSET} could be used.</li>
   *   <li>Charset may depends on sibling file's. For example, JSP charset can be evaluated using <i>web.xml</i>.
   *     In this case {@link CharsetHint#NO_HINT} should be used.</li>
   * </ul>
   */
  @ApiStatus.Experimental
  @NotNull
  default CharsetHint getCharsetHint() {
    return CharsetHint.NO_HINT;
  }

  @ApiStatus.Experimental
  @ApiStatus.NonExtendable
  interface CharsetHint {
    final class ForcedCharset implements CharsetHint {
      @NotNull
      private final Charset myCharset;

      @NotNull
      public ForcedCharset(@NotNull Charset charset) {myCharset = charset;}

      @NotNull
      public Charset getCharset() {
        return myCharset;
      }
    }

    CharsetHint CONTENT_DEPENDENT_CHARSET = new CharsetHint() {};
    CharsetHint NO_HINT = new CharsetHint() {};
  }
}
