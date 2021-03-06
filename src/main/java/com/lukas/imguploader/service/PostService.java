package com.lukas.imguploader.service;


import com.lukas.imguploader.dto.PostDTO;
import com.lukas.imguploader.entity.ImageModel;
import com.lukas.imguploader.entity.Post;
import com.lukas.imguploader.entity.User;
import com.lukas.imguploader.exceptions.PostNotFoundException;
import com.lukas.imguploader.repository.ImageRepository;
import com.lukas.imguploader.repository.PostRepository;
import com.lukas.imguploader.repository.UserRepository;
import java.security.Principal;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PostService {

  public static final Logger LOG = LoggerFactory.getLogger(PostService.class);

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final ImageRepository imageRepository;

  @Autowired
  public PostService(PostRepository postRepository, UserRepository userRepository,
      ImageRepository imageRepository) {
    this.postRepository = postRepository;
    this.userRepository = userRepository;
    this.imageRepository = imageRepository;
  }

  public Post createPost(PostDTO postDTO, Principal principal) {
    User user = getUserByPrincipal(principal);
    Post post = new Post();
    post.setUser(user);
    post.setCaption(postDTO.getCaption());
    post.setLocation(postDTO.getLocation());
    post.setTitle(postDTO.getTitle());
    post.setLikes(0);

    LOG.info("Saving Post for User: {}", user.getEmail());
    return postRepository.save(post);
  }

  public List<Post> getAllPosts() {
    return postRepository.findAllByOrderByCreatedDateDesc();
  }

  public Post getPostById(Long postId, Principal principal) {
    User user = getUserByPrincipal(principal);
    return postRepository.findPostByIdAndUser(postId, user)
        .orElseThrow(() -> new PostNotFoundException(
            "Post cannot be found for username: " + user.getEmail()));
  }

  public List<Post> getAllPostForUser(Principal principal) {
    User user = getUserByPrincipal(principal);
    return postRepository.findAllByUserOrderByCreatedDateDesc(user);
  }

  public Post likePost(Long postId, String username) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new PostNotFoundException("Post cannot be found"));

    Optional<String> userLiked = post.getLikedUsers()
        .stream()
        .filter(u -> u.equals(username)).findAny();

    if (userLiked.isPresent()) {
      post.setLikes(post.getLikes() - 1);
      post.getLikedUsers().remove(username);
    } else {
      post.setLikes(post.getLikes() + 1);
      post.getLikedUsers().add(username);
    }
    return postRepository.save(post);
  }

  public void deletePost(Long postId, Principal principal) {
    Post post = getPostById(postId, principal);
    Optional<ImageModel> imageModel = imageRepository.findByPostId(post.getId());
    postRepository.delete(post);
    imageModel.ifPresent(imageRepository::delete);
  }

  private User getUserByPrincipal(Principal principal) {
    String username = principal.getName();
    return userRepository.findUserByUsername(username)
        .orElseThrow(
            () -> new UsernameNotFoundException("Username not found with username " + username));

  }

  public Post updatePost(Post post, Principal principal) {
    Post postById = getPostById(post.getId(), principal);
    postById.setTitle(post.getTitle());
    postById.setCaption(post.getCaption());
    postById.setLocation(post.getLocation());
    Optional<ImageModel> imageModel = imageRepository.findByPostId(post.getId());
    Post savePost = postRepository.save(postById);
    imageModel.ifPresent(imageRepository::save);
    return savePost;
  }
}
