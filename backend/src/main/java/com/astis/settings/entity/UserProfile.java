package com.astis.settings.entity;

import com.astis.user.entity.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(name = "study_pace", nullable = false, length = 20)
    private StudyPace studyPace;

    @Enumerated(EnumType.STRING)
    @Column(name = "deadline_pressure_tolerance", nullable = false, length = 20)
    private DeadlinePressureTolerance deadlinePressureTolerance;

    @Enumerated(EnumType.STRING)
    @Column(name = "daily_study_capacity", nullable = false, length = 20)
    private DailyStudyCapacity dailyStudyCapacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "preferred_study_time", nullable = false, length = 20)
    private PreferredStudyTime preferredStudyTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "planning_style", nullable = false, length = 20)
    private PlanningStyle planningStyle;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected UserProfile() {
    }

    public UserProfile(AppUser user) {
        this.user = user;
        this.studyPace = StudyPace.NORMAL;
        this.deadlinePressureTolerance = DeadlinePressureTolerance.MEDIUM;
        this.dailyStudyCapacity = DailyStudyCapacity.MEDIUM;
        this.preferredStudyTime = PreferredStudyTime.EVENING;
        this.planningStyle = PlanningStyle.BALANCED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    public void update(
            StudyPace studyPace,
            DeadlinePressureTolerance deadlinePressureTolerance,
            DailyStudyCapacity dailyStudyCapacity,
            PreferredStudyTime preferredStudyTime,
            PlanningStyle planningStyle
    ) {
        this.studyPace = studyPace;
        this.deadlinePressureTolerance = deadlinePressureTolerance;
        this.dailyStudyCapacity = dailyStudyCapacity;
        this.preferredStudyTime = preferredStudyTime;
        this.planningStyle = planningStyle;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public AppUser getUser() {
        return user;
    }

    public StudyPace getStudyPace() {
        return studyPace;
    }

    public DeadlinePressureTolerance getDeadlinePressureTolerance() {
        return deadlinePressureTolerance;
    }

    public DailyStudyCapacity getDailyStudyCapacity() {
        return dailyStudyCapacity;
    }

    public PreferredStudyTime getPreferredStudyTime() {
        return preferredStudyTime;
    }

    public PlanningStyle getPlanningStyle() {
        return planningStyle;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
