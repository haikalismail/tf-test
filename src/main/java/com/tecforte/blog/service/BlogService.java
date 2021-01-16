package com.tecforte.blog.service;

import com.tecforte.blog.domain.Blog;
import com.tecforte.blog.repository.BlogRepository;
import com.tecforte.blog.repository.EntryRepository;
import com.tecforte.blog.service.dto.BlogDTO;
import com.tecforte.blog.service.dto.EntryDTO;
import com.tecforte.blog.service.mapper.BlogMapper;
import com.tecforte.blog.service.mapper.EntryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service Implementation for managing {@link Blog}.
 */
@Service
@Transactional
public class BlogService {

    private final Logger log = LoggerFactory.getLogger(BlogService.class);

    private final BlogRepository blogRepository;
    
    private final EntryRepository entryRepository;

    private final BlogMapper blogMapper;
    
    private final EntryMapper entryMapper;

    public BlogService(BlogRepository blogRepository, BlogMapper blogMapper,EntryRepository entryRepository,EntryMapper entryMapper) {
        this.blogRepository = blogRepository;
        this.blogMapper = blogMapper;
        this.entryRepository = entryRepository;
        this.entryMapper = entryMapper;
    }

    /**
     * Save a blog.
     *
     * @param blogDTO the entity to save.
     * @return the persisted entity.
     */
    public BlogDTO save(BlogDTO blogDTO) {
        log.debug("Request to save Blog : {}", blogDTO);
        Blog blog = blogMapper.toEntity(blogDTO);
        blog = blogRepository.save(blog);
        return blogMapper.toDto(blog);
    }

    /**
     * Get all the blogs.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<BlogDTO> findAll() {
        log.debug("Request to get all Blogs");
        return blogRepository.findAll().stream()
            .map(blogMapper::toDto)
            .collect(Collectors.toCollection(LinkedList::new));
    }


    /**
     * Get one blog by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<BlogDTO> findOne(Long id) {
        log.debug("Request to get Blog : {}", id);
        return blogRepository.findById(id)
            .map(blogMapper::toDto);
    }

    /**
     * Delete the blog by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Blog : {}", id);
        blogRepository.deleteById(id);
    }
    
    /**
     * Delete the blog entry by keyword.
     *
     */
    public void deleteBlogEntry(List<String> keyword) {
        log.debug("Request to delete Blog Entry based on keyword: {}", keyword);
        
        List<EntryDTO> entryDTO = entryRepository.findAll().stream()
                .map(entryMapper::toDto)
                .collect(Collectors.toCollection(LinkedList::new));
        
        for(EntryDTO entry:entryDTO){
            List<String> splitContent = Arrays.asList(entry.getContent().split(" "));
            for(int i=0; i < splitContent.size();i++){
                if(keyword.contains(splitContent.get(i))){
                    entryRepository.deleteById(entry.getId());
                }
            }
        }
    }
    
    /**
     * Delete the blog entry by keyword and blog id.
     *
     */
    public void deleteBlogIdEntry(Long id, List<String> keyword) {
        log.debug("Request to delete Blog Entry based on id and keywords: {}", keyword);
        
        List<EntryDTO> entryDTO = entryRepository.findAll().stream()
                .map(entryMapper::toDto)
                .collect(Collectors.toCollection(LinkedList::new));
        for(EntryDTO entry:entryDTO){
            if(entry.getBlogId().equals(id)){
                List<String> splitContent = Arrays.asList(entry.getContent().split(" "));
                for(int i=0; i < splitContent.size();i++){
                    if(keyword.contains(splitContent.get(i))){
                        entryRepository.deleteById(entry.getId());
                    }
                }
            }
        }
    }
}
