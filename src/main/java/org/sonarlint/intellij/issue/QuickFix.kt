/*
 * SonarLint for IntelliJ IDEA
 * Copyright (C) 2015-2021 SonarSource
 * sonarlint@sonarsource.com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonarlint.intellij.issue

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.RangeMarker
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import org.sonarlint.intellij.common.ui.SonarLintConsole
import org.sonarlint.intellij.util.getDocument
import org.sonarsource.sonarlint.core.client.api.common.ClientInputFileEdit
import org.sonarsource.sonarlint.core.client.api.common.TextEdit
import org.sonarsource.sonarlint.core.client.api.common.QuickFix as CoreQuickFix

fun convert(project: Project, coreQuickFix: CoreQuickFix): QuickFix? {
    val virtualFileEdits = coreQuickFix.inputFileEdits().map { convert(it) }
    if (virtualFileEdits.contains(null)) {
        log(project, "Quick fix won't be proposed because some documents cannot be found")
        return null
    }
    if (virtualFileEdits.distinctBy { it!!.target }.size > 1) {
        log(project, "Quick fix won't be proposed because multi-files edits are not supported")
        return null
    }
    return QuickFix(coreQuickFix.message(), virtualFileEdits.mapNotNull { it })
}

private fun log(project: Project, message: String) {
    SonarLintConsole.get(project).debug(message)
}

private fun convert(fileEdit: ClientInputFileEdit): VirtualFileEdit? {
    val targetVirtualFile = fileEdit.target().getClientObject<VirtualFile>()
    val document = targetVirtualFile.getDocument() ?: return null
    return VirtualFileEdit(targetVirtualFile, fileEdit.textEdits().map { convert(document, it) })
}

private fun convert(document: Document, textEdit: TextEdit): RangeMarkerEdit {
    val range = textEdit.range()
    val startOffset = document.getLineStartOffset(range.start().line() - 1) + range.start().lineOffset()
    val endOffset = document.getLineStartOffset(range.end().line() - 1) + range.end().lineOffset()
    // XXX should we dispose them at some point ?
    val rangeMarker = document.createRangeMarker(startOffset, endOffset)
    return RangeMarkerEdit(rangeMarker, textEdit.newText())
}

data class QuickFix(val message: String, val virtualFileEdits: List<VirtualFileEdit>) {
    var applied = false
    fun isApplicable() =
        !applied && virtualFileEdits.all { it.target.isValid && it.edits.all { e -> e.rangeMarker.isValid } }
}

data class VirtualFileEdit(val target: VirtualFile, val edits: List<RangeMarkerEdit>)

data class RangeMarkerEdit(val rangeMarker: RangeMarker, val newText: String)
