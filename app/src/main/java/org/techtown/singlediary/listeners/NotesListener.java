package org.techtown.singlediary.listeners;

import org.techtown.singlediary.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
