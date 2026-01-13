-- Script to populate imageResourceName for existing categories
-- Run this after the backend starts and the image_resource_name column is added

UPDATE categories SET image_resource_name = 'activity_sports' WHERE LOWER(name) = 'sports';
UPDATE categories SET image_resource_name = 'activity_social' WHERE LOWER(name) = 'social';
UPDATE categories SET image_resource_name = 'activity_outdoor' WHERE LOWER(name) = 'outdoor';
UPDATE categories SET image_resource_name = 'activity_food' WHERE LOWER(name) = 'food';
UPDATE categories SET image_resource_name = 'activity_travel' WHERE LOWER(name) = 'travel';
UPDATE categories SET image_resource_name = 'activity_photography' WHERE LOWER(name) = 'photography';
UPDATE categories SET image_resource_name = 'activity_music' WHERE LOWER(name) = 'music';
UPDATE categories SET image_resource_name = 'activity_art' WHERE LOWER(name) = 'art';
UPDATE categories SET image_resource_name = 'activity_gaming' WHERE LOWER(name) = 'gaming';
UPDATE categories SET image_resource_name = 'activity_fitness' WHERE LOWER(name) = 'fitness';

-- Set a default image for any categories that don't match the above
UPDATE categories SET image_resource_name = 'activity_default' WHERE image_resource_name IS NULL;
