package searchengine.services.site;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import searchengine.model.Site;
import searchengine.model.SiteStatus;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SiteService {
    private final SiteRepository siteRepository;

    @Transactional
    public void failed(Integer siteId, String error) {
        log.warn("Failed indexing site with id {}: {}", siteId, error);
        Site site = getPersistSite(siteId);
        site.setLastError(error);
        site.setStatus(SiteStatus.FAILED);
    }

    public boolean isNotFailed(Integer siteId) {
        return !siteRepository.existsByIdAndStatus(siteId, SiteStatus.FAILED);
    }

    @Transactional
    public void updateStatusTime(Integer siteId) {
        Site site = getPersistSite(siteId);
        site.setStatusTime(LocalDateTime.now());
    }

    @Transactional
    public void indexed(Integer siteId) {
        Site site = getPersistSite(siteId);
        site.setStatusTime(LocalDateTime.now());
        site.setStatus(SiteStatus.INDEXED);
    }

    @Transactional
    public void indexing(Integer siteId) {
        Site site = getPersistSite(siteId);
        site.setStatus(SiteStatus.INDEXING);
        log.info("Site indexing: {}", site);
    }

    private Site getPersistSite(Integer siteId) {
        return siteRepository.findById(siteId).orElseThrow(() -> new IllegalStateException("Site not found"));
    }

    @Transactional
    public void save(String name, String url) {
        siteRepository.save(Site.builder()
                .name(name)
                .status(SiteStatus.INDEXING)
                .url(url.toLowerCase())
                .statusTime(LocalDateTime.now())
                .build());
    }

    public List<Site> findAll() {
        return siteRepository.findAll();
    }

    @Transactional
    public void deleteAll() {
        siteRepository.deleteAllInBatch();
    }

    @Transactional
    public void stopIndexingAllSites() {
        siteRepository.findAllByStatus(SiteStatus.INDEXING).forEach(site -> {
            site.setLastError("Индексация остановлена пользователем");
            site.setStatus(SiteStatus.FAILED);
        });
    }

    public Optional<Site> findByUrlIgnoreCase(String url) {
        return siteRepository.findByUrlIgnoreCase(url);
    }

    public boolean existsIndexingSite() {
        return siteRepository.existsByStatus(SiteStatus.INDEXING);
    }
}
