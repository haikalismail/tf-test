package com.tecforte.blog.service;

import com.tecforte.blog.domain.Entry;
import com.tecforte.blog.repository.EntryRepository;
import com.tecforte.blog.service.dto.EntryDTO;
import com.tecforte.blog.service.dto.BlogDTO;
import com.tecforte.blog.service.mapper.EntryMapper;
import com.tecforte.blog.service.BlogService;
import com.tecforte.blog.domain.enumeration.Emoji;
import com.tecforte.blog.web.rest.errors.BadRequestAlertException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Arrays;
import java.util.List;

import java.lang.Object;

/**
 * Service Implementation for managing {@link Entry}.
 */
@Service
@Transactional
public class EntryService {

    private final Logger log = LoggerFactory.getLogger(EntryService.class);

    private final EntryRepository entryRepository;

    private final EntryMapper entryMapper;
    
    private final BlogService blogService;

    private static final String ENTITY_NAME = "entry";
    
    public EntryService(EntryRepository entryRepository, EntryMapper entryMapper,
                        BlogService blogService) {
        this.entryRepository = entryRepository;
        this.entryMapper = entryMapper;
        this.blogService = blogService;
    }

    /**
     * Save a entry.
     *
     * @param entryDTO the entity to save.
     * @return the persisted entity.
     */
    public EntryDTO save(EntryDTO entryDTO) {
        log.debug("Request to save Entry : {}", entryDTO);
        
        List<String> emojiPositive = Arrays.asList("LIKE","HAHA","WOW"),
                     emojiNegative = Arrays.asList("SAD","ANGRY","WOW"),
                     keywordPositive = Arrays.asList("like","love","happy","haha","laugh"),
                     keywordNegative = Arrays.asList("angry","sad","fear","cry","lonely");
        
        Optional<BlogDTO> blogDTO = blogService.findOne(entryDTO.getBlogId());
        Entry entry = entryMapper.toEntity(entryDTO);
        
        if(blogDTO.get().isPositive()){
           entry = saveEntry("positive",entryDTO,emojiPositive,keywordPositive,keywordNegative,entry);
           
        } else {
           entry = saveEntry("negative",entryDTO,emojiNegative,keywordPositive,keywordNegative,entry);
        }
        return entryMapper.toDto(entry);
    }
    
    public Entry saveEntry(String blogType,
                           EntryDTO entryDTO,
                           List<String> emoji, 
                           List<String> keywordPositive, 
                           List<String> keywordNegative, 
                           Entry entry) {
        boolean emojiCheck = emoji.contains(entryDTO.getEmoji().toString()),
                keywordCheck = false;
        List<String> splitContent = Arrays.asList(entryDTO.getContent().split(" "));
        
        for(int i=0; i < splitContent.size();i++){
            log.debug("i : {}", i);
            log.debug("splitContent : {}", splitContent);
            if(blogType.equals("positive")){
                if(keywordPositive.contains(splitContent.get(i).toLowerCase())){
                    keywordCheck = true;
                } else if (keywordNegative.contains(splitContent.get(i).toLowerCase())){
                    log.debug("keyword : {}", splitContent.get(i).toLowerCase());
                    keywordCheck = false;
                    break;
                } else {
                    keywordCheck = true;
                }
            } else {
                if(keywordNegative.contains(splitContent.get(i).toLowerCase())){
                    keywordCheck = true;
                } else if (keywordPositive.contains(splitContent.get(i).toLowerCase())){
                    keywordCheck = false;
                    break;
                } else {
                    keywordCheck = true;
                }
            }
        }
        
        if(!emojiCheck){
            throw new BadRequestAlertException(
                    "Invalid Emoji", ENTITY_NAME, "invalidEmoji");
        } else if (!keywordCheck) {
            throw new BadRequestAlertException(
                    "Invalid Content", ENTITY_NAME, "invalidContent");
        } else {
            return entryRepository.save(entry);
        }
    }
    
    /**
     * Get all the entries.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public Page<EntryDTO> findAll(Pageable pageable) {
        log.debug("Request to get all Entries");
        return entryRepository.findAll(pageable)
            .map(entryMapper::toDto);
    }


    /**
     * Get one entry by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<EntryDTO> findOne(Long id) {
        log.debug("Request to get Entry : {}", id);
        return entryRepository.findById(id)
            .map(entryMapper::toDto);
    }

    /**
     * Delete the entry by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        log.debug("Request to delete Entry : {}", id);
        entryRepository.deleteById(id);
    }
}
