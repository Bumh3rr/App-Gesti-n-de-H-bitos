package com.app.data.local.entity;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import java.util.Date;

@Entity(
        tableName = "habit_completions",
        foreignKeys = @ForeignKey(
                entity = HabitEntity.class,
                parentColumns = "id",
                childColumns = "habitId",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index("habitId"), @Index("date")}
)
public class HabitCompletionEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;
    private long habitId;
    private String date; // Format: yyyy-MM-dd
    private String status; // completed, partial, skipped
    private int progress; // 0-100
    private String details;
    private Date completedAt;

    public HabitCompletionEntity() {
        this.completedAt = new Date();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getHabitId() { return habitId; }
    public void setHabitId(long habitId) { this.habitId = habitId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getProgress() { return progress; }
    public void setProgress(int progress) { this.progress = progress; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Date getCompletedAt() { return completedAt; }
    public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }
}
