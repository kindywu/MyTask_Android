# Task Management App — Design Spec

**Date:** 2026-06-03
**Status:** Approved

## Overview

A personal task management Android app with 4-digit PIN login and SQLite storage. Built with Jetpack Compose, Material 3, Room, Navigation Compose, and WorkManager.

## Feature Summary

- PIN lock required on every app open (4-digit, hash-stored in DataStore)
- Full CRUD tasks with title, notes, priority (high/medium/low), due date, custom reminders
- Infinite nested subtasks via adjacency list (parent_task_id)
- User-customizable categories with color, single category per task
- Search by title+notes, filter by category/priority/status/due-date range, sort by multiple fields
- Local notifications for task reminders via WorkManager

## Architecture

```
UI (Compose) → ViewModel (StateFlow) → Repository → Room (SQLite)
                                                  → DataStore (PIN)
                                                  → WorkManager (reminders)
```

Single Activity, Navigation Compose for screen routing.

### Navigation

```
PINLock → TaskList → TaskDetail → EditTask
                  → Search
                  → Categories → EditCategory
                  → Settings (change PIN)
```

## Data Model

### Task (Room Entity)
| Field          | Type      | Notes                              |
|----------------|-----------|------------------------------------|
| id             | Long (PK) | auto-generate                      |
| title          | String    | required                           |
| notes          | String    | default ""                         |
| priority       | Enum      | HIGH / MEDIUM / LOW                |
| isCompleted    | Boolean   | default false                      |
| dueDate        | Long?     | epoch millis, null = no due date   |
| reminderMinutes| Int?      | minutes before due, null = no rem. |
| categoryId     | Long?     | FK → Category, null = uncategorized|
| parentTaskId   | Long?     | FK → self, null = root task        |
| sortOrder      | Int       | sibling ordering                   |
| createdAt      | Long      | epoch millis                       |
| updatedAt      | Long      | epoch millis                       |

### Category (Room Entity)
| Field     | Type      | Notes            |
|-----------|-----------|------------------|
| id        | Long (PK) | auto-generate    |
| name      | String    | required         |
| color     | Int       | ARGB color int   |
| sortOrder | Int       |                  |
| createdAt | Long      | epoch millis     |

### Subtask Tree
- Adjacency list: `parentTaskId` references parent's `id`
- Repository recursively assembles tree in memory
- Delete parent cascades to all descendants (Repository handles)

### PIN
- DataStore: SHA-256 hash + random salt
- 4-digit numeric only

### Reminders
- WorkManager OneTimeWorkRequest scheduled per task
- Task update → cancel old work, schedule new
- Notification opens task detail on tap

## Screens

### 1. PIN Lock
- Numeric keypad, 4-dot indicator
- First launch: "Set PIN", enter twice to confirm
- Wrong PIN: dot shake animation + "PIN incorrect"
- No recovery — personal tool, reinstall to reset

### 2. Task List (Home)
- Top bar: title + search icon + overflow menu (categories, settings)
- Filter chips: All | Today | This Week | High Priority
- Grouped: incomplete (by priority) → completed (collapsible)
- FAB → new task
- Each card: checkbox + title + due date + priority dot + category chip + subtask count
- Swipe-left: edit / delete / add subtask

### 3. Task Detail / Edit
- Title field, notes field (multiline)
- Priority segment control (3 options)
- Category picker (bottom sheet)
- Date + time pickers for due date
- Reminder picker: None / At due time / 5min / 15min / 30min / 1hr / 1day / Custom
- Subtask list (expandable, reorderable)
- Add subtask button
- Save / Delete buttons
- Created/updated timestamps (small, bottom)

### 4. Search & Filter
- Search bar (title + notes)
- Filters: category, priority, completion status, date range
- Sort: due date / priority / created / title A-Z
- Results list (same card style as task list)

### 5. Categories
- List of categories with edit/delete
- FAB → new category (name + color picker)
- Delete warns: "N tasks will become uncategorized"

### 6. Settings
- Change PIN (verify old PIN first)
- About

## Error Handling
- Room operations wrapped in Result<T>
- UI errors via Snackbar
- Input validation in ViewModel (title required, PIN 4 digits)
- Empty states for: no tasks, no search results, no categories

## Testing
- Repository & ViewModel: JUnit + Turbine (StateFlow)
- DAO: Room instrumentation tests
- Compose UI: no isolated tests (high churn, low value for personal tool)

## Dependencies to Add
- `androidx.navigation:navigation-compose`
- `androidx.room:room-runtime`, `room-ktx`, `room-compiler`
- `androidx.datastore:datastore-preferences`
- `androidx.work:work-runtime-ktx`
- `com.google.accompanist:accompanist-permissions` (notification permission)
