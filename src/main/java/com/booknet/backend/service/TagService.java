package com.booknet.backend.service;

import com.booknet.backend.dto.CreateTagRequest;
import com.booknet.backend.dto.TagResponse;
import com.booknet.backend.dto.UpdateTagRequest;
import com.booknet.backend.model.Tag;
import com.booknet.backend.repository.TagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TagService {

    @Autowired
    private TagRepository tagRepository;

    public TagResponse createTag(CreateTagRequest request) {
        Tag tag = new Tag();
        tag.setName(request.getNombre());
        tag.setCategory(request.getCategoria());
        
        Tag savedTag = tagRepository.save(tag);
        return convertToTagResponse(savedTag);
    }

    public Optional<TagResponse> getTagById(String id) {
        return tagRepository.findById(id)
                .map(this::convertToTagResponse);
    }

    public List<TagResponse> getAllTagsWithPagination(int offset, int limit) {
        List<Tag> tags = tagRepository.findAllOrderByCreatedAtDescWithPagination(offset, limit);
        return tags.stream()
                .map(this::convertToTagResponse)
                .collect(Collectors.toList());
    }

    public long getTotalTagsCount() {
        return tagRepository.countAllTags();
    }

    // Obtener todos los tags sin paginación
    public List<TagResponse> getAllTags() {
        List<Tag> tags = tagRepository.findAll();
        return tags.stream()
                .map(this::convertToTagResponse)
                .collect(Collectors.toList());
    }

    public Optional<TagResponse> updateTag(String id, UpdateTagRequest request) {
        Optional<Tag> tagOptional = tagRepository.findById(id);
        
        if (tagOptional.isPresent()) {
            Tag tag = tagOptional.get();
            
            // Actualizar solo los campos que no son null
            if (request.getNombre() != null) {
                tag.setName(request.getNombre());
            }
            if (request.getCategoria() != null) {
                tag.setCategory(request.getCategoria());
            }
            
            tag.setUpdatedAt(LocalDateTime.now());
            Tag updatedTag = tagRepository.save(tag);
            return Optional.of(convertToTagResponse(updatedTag));
        }
        
        return Optional.empty();
    }

    public boolean deleteTag(String id) {
        Optional<Tag> tagOptional = tagRepository.findById(id);
        
        if (tagOptional.isPresent()) {
            Tag tag = tagOptional.get();
            
            // Verificar si el tag tiene libros asociados
            if (tag.getBooks() != null && !tag.getBooks().isEmpty()) {
                throw new RuntimeException("No se puede eliminar el tag porque tiene libros asociados");
            }
            
            tagRepository.deleteById(id);
            return true;
        }
        
        return false;
    }

    public TagResponse convertToTagResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setId(tag.getId());
        response.setNombre(tag.getName());
        response.setCategoria(tag.getCategory());
        return response;
    }
}
