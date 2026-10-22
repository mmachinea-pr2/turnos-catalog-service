package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.DayOfWeek;
import java.time.LocalTime;

@Entity
@Table(name = "weekly_schedule")
public class WeeklyScheduleEntity {
    @Id
    private Long id;

    @Column(nullable = false)
    private Long professionalId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 9)
    private DayOfWeek dayOfWeek;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column(nullable = false)
    private LocalTime endTime;

    @Column(nullable = false)
    private int slotDurationMinutes;

    @Column(nullable = false)
    private boolean enabled;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected WeeklyScheduleEntity() {}

    public WeeklyScheduleEntity(Long id, Long professionalId, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime, int slotDurationMinutes, boolean enabled, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.professionalId = professionalId;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
        this.slotDurationMinutes = slotDurationMinutes;
        this.enabled = enabled;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public Long getProfessionalId() { return professionalId; }
    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public int getSlotDurationMinutes() { return slotDurationMinutes; }
    public boolean getEnabled() { return enabled; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
