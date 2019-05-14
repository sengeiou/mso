#ifndef PASARM_TIMER_H__
#define PASARM_TIMER_H__

#include "boards.h"
#include "nrf_drv_timer.h"

uint32_t get_PasArm_timer_count();
void set_PasArm_timer_reset(bool timer_reset);
void set_PasArm_Timer_Event(bool timer_event);
bool get_PasArm_Timer_Event();
void set_measure(bool measure);

void timer_puls_event_handler(nrf_timer_event_t event_type, void* p_context);
void puls_timer_init();

#endif // PASARM_TIMER_H__