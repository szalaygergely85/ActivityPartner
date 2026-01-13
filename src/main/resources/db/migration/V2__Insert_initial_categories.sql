-- Insert initial categories with image resource names
INSERT INTO categories (name, description, icon, image_resource_name, is_active, activity_count, created_at) VALUES
('Sports', 'Sports and athletic activities', '⚽', 'activity_sports', true, 0, NOW()),
('Social', 'Social gatherings and meetups', '👥', 'activity_social', true, 0, NOW()),
('Outdoor', 'Outdoor adventures and nature activities', '🏕️', 'activity_outdoor', true, 0, NOW()),
('Food', 'Food tastings, cooking, and dining experiences', '🍽️', 'activity_food', true, 0, NOW()),
('Travel', 'Travel and exploration activities', '✈️', 'activity_travel', true, 0, NOW()),
('Photography', 'Photography walks and photo sessions', '📷', 'activity_photography', true, 0, NOW()),
('Music', 'Music events, concerts, and jam sessions', '🎵', 'activity_music', true, 0, NOW()),
('Art', 'Art exhibitions, workshops, and creative activities', '🎨', 'activity_art', true, 0, NOW()),
('Gaming', 'Gaming sessions and esports', '🎮', 'activity_gaming', true, 0, NOW()),
('Fitness', 'Fitness classes and workout sessions', '💪', 'activity_fitness', true, 0, NOW())
ON DUPLICATE KEY UPDATE
    image_resource_name = VALUES(image_resource_name),
    icon = VALUES(icon),
    description = VALUES(description);
