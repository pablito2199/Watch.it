import { BanOutline as DeclineRequest, CheckCircleOutline as AcceptRequest, CalendarOutline as Calendar, LocationMarkerOutline as Location } from '@graywolfai/react-heroicons'
import { useState } from 'react'
import { Shell, Separator } from '../../components'

import { useUser, useFriends } from '../../hooks'

export default function Profile() {
    const { user, createUser, updateUser } = useUser()

    return <Shell>
        <div className='mx-auto w-full max-w-screen-2xl p-8'>
            <img
                style={{ height: '36rem' }}
                src={user.picture}
                alt={user.name}
                className='absolute top-2 left-0 right-0 w-full object-cover filter blur transform scale-105'
            />
            <Header user={user} />
            <PendingFriendships user={user} />
            <AcceptedFriendships user={user} />
        </div>
    </Shell>
}

function Header({ user }) {
    return <header className='mt-96 relative flex pb-8 mb-8'>
        <img style={{ aspectRatio: '1/1' }}
            src={user.picture}
            alt={user.name}
            className='absolute w-64 rounded-full shadow-xl z-20' />
        <hgroup className='ml-12 flex-1 mt-28'>
            <h1 className={`bg-black bg-opacity-50 backdrop-filter backdrop-blur 
                                      text-right text-white text-6xl font-bold
                                      p-6`}>
                {user.name}
            </h1>
            <Info user={user} />
        </hgroup>
    </header>
}
function Info({ user }) {
    return <div className='flex justify-between'>
        <div className='ml-60 flex'>
            <Calendar className='h-12 w-12 mt-2' />
            <span className={`self-centerblock text-2xl font-semibold text-black w-full py-4 text-right`}>
                {
                    user.birthday && <>{user.birthday.day}/{user.birthday.month}/{user.birthday.year}</>
                }
            </span>
        </div>
        <div className='flex ml-60'>
            <Location className='h-12 w-12 mt-2' />
            <span className={`self-centerblock text-2xl font-semibold text-black w-full py-4 text-right`}>
                {user.country}
            </span>
        </div>
        <span className={`block text-3xl font-semibold text-black w-full px-8 py-4 text-right`}>
            {user.email}
        </span>
    </div>
}

function PendingFriendships({ user }) {
    return <>
        <h2 className='mt-16 font-bold text-2xl'>Solicitudes de amistad</h2>
        <Separator />
        <div className='flex flex-col'>
            <ObtainFriendsNotAccepted user={user} />
        </div>
    </>
}

function ObtainFriendsNotAccepted({ user }) {
    const { friends } = useFriends(user.email)

    let render = <></>

    if (friends != null && friends.content != null) {
        render = friends.content.map((friendship) =>
            friendship.confirmed === false
            &&
            (
                friendship.user === user.email
                    ?
                    <div key={friendship.id} className='w-full ml-4 mt-6 h-24 bg-white rounded p-4 flex justify-between shadow-md border-2' style={{ maxWidth: '450px' }}>
                        <span className='ml-4 mt-4 font-bold'>{friendship.friend}</span>
                        <div className='flex mt-3 mr-3'>
                            <DeclineRequest
                                className='w-8 h-8 mr-2'
                            />
                            <AcceptRequest
                                className='w-8 h-8'
                            />
                        </div>
                    </div>
                    :
                    <div key={friendship.id} className='ml-4 mt-6 h-24 bg-white rounded p-4 flex justify-between shadow-md border-2' style={{ maxWidth: '420px' }}>
                        <span className='ml-4 mt-4 font-bold'>{friendship.user}</span>
                        <div className='flex mt-3 mr-3'>
                            <DeclineRequest
                                className='w-8 h-8 mr-2'
                            />
                            <AcceptRequest
                                className='w-8 h-8'
                            />
                        </div>
                    </div>
            )
        );
    }

    return render
}

function AcceptedFriendships({ user }) {

    return <>
        <h2 className='mt-16 font-bold text-2xl'>Amigos</h2>
        <Separator />
        <div>
            <ObtainFriendsAccepted user={user} />
        </div>
    </>
}

function ObtainFriendsAccepted({ user }) {
    const { friends } = useFriends(user.email)

    let render = <></>

    if (friends != null && friends.content != null) {
        /*render = friends.content.map((friendship) =>
            friendship.confirmed === true
            &&
            (
                friendship.user === user.email
                    ?
                    <div key={friendship.friend} className='mt-12 h-96 bg-white rounded p-4 flex flex-col shadow-md border-2' style={{ minWidth: '900px' }}>
                        <div className='ml-8 mt-4 flex justify-between'>
                            <span className='font-bold'>{friendship.friend}</span>
                        </div>
                    </div>
                    :
                    <div key={friendship.user} className='mt-12 h-96 bg-white rounded p-4 flex flex-col shadow-md border-2' style={{ minWidth: '900px' }}>
                        <div className='ml-8 mt-4 flex justify-between'>
                            <span className='font-bold'>{friendship.user}</span>
                        </div>
                    </div>
            )
        );*/
    }

    return render
}