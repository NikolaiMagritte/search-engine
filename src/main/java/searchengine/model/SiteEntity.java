package searchengine.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "site")
public class SiteEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Basic(optional = false)
    private SiteStatus status;

    @Column(name = "status_time", nullable = false)
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(nullable = false)
    private String url;

    @Column(nullable = false)
    private String name;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "siteId", cascade = CascadeType.ALL)
    protected List<PageEntity> pageList = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "siteId", cascade = CascadeType.ALL)
    protected List<LemmaEntity> lemmaEntityList = new ArrayList<>();
}
