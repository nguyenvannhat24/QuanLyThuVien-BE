package com.dev.book.service;

import com.dev.book.dto.PublisherRequest;
import com.dev.book.dto.PublisherResponse;
import com.dev.book.model.Publisher;
import com.dev.book.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublisherServiceImpl implements PublisherService {

    private final PublisherRepository publisherRepository;

    @Override
    public PublisherResponse create(PublisherRequest request) {
        Publisher publisher = Publisher.builder()
                .publisherName(request.getPublisherName())
                .address(request.getAddress())
                .phone(request.getPhone())
                .email(request.getEmail())
                .build();

        publisherRepository.save(publisher);
        return mapToResponse(publisher);
    }

    @Override
    public List<PublisherResponse> getAll() {
        return publisherRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PublisherResponse getById(Long id) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publisher not found"));
        return mapToResponse(publisher);
    }

    @Override
    public PublisherResponse update(Long id, PublisherRequest request) {
        Publisher publisher = publisherRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Publisher not found"));

        publisher.setPublisherName(request.getPublisherName());
        publisher.setAddress(request.getAddress());
        publisher.setPhone(request.getPhone());
        publisher.setEmail(request.getEmail());

        publisherRepository.save(publisher);
        return mapToResponse(publisher);
    }

    @Override
    public void delete(Long id) {
        publisherRepository.deleteById(id);
    }

    private PublisherResponse mapToResponse(Publisher publisher) {
        return PublisherResponse.builder()
                .id(publisher.getId())
                .publisherName(publisher.getPublisherName())
                .address(publisher.getAddress())
                .phone(publisher.getPhone())
                .email(publisher.getEmail())
                .createdAt(publisher.getCreatedAt())
                .build();
    }
}
